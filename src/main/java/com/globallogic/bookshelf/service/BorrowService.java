package com.globallogic.bookshelf.service;

import com.globallogic.bookshelf.entity.Book;
import com.globallogic.bookshelf.entity.Borrow;
import com.globallogic.bookshelf.exeptions.BookshelfConflictException;
import com.globallogic.bookshelf.exeptions.BookshelfResourceNotFoundException;
import com.globallogic.bookshelf.repository.BookRepository;
import com.globallogic.bookshelf.repository.BorrowRepository;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.*;

/**
 * Business logic of the /borrow request
 *
 * @author Bartłomiej Chojnacki
 */

@Component
public class BorrowService {

    protected BorrowRepository borrowRepository;
    protected BookRepository bookRepository;

    public BorrowService(BorrowRepository bwRepository, BookRepository bkRepository) {
        borrowRepository = bwRepository;
        bookRepository = bkRepository;
    }


    /**
     * Create a borrow
     *
     * @param borrowBody
     * @return String.format informing that book was borrowed
     * @throws BookshelfConflictException exception informing that book is already borrowed
     */
    @Transactional
    public void borrowBook(Borrow borrowBody) {
        Book book = bookRepository.findById(borrowBody.getBook().getId()).get();
        if (book.isAvailable()) {
            book.setAvailable(false);
            bookRepository.save(book);
            if (borrowBody.getBorrowed() == null) {

                borrowBody.setBorrowed(new Date());
            }
            borrowRepository.save(borrowBody);
        } else {
            throw new BookshelfConflictException(String.format("Book with name : %s is already borrowed.", book.getName()));
        }
    }

    /**
     * Return a book
     *
     * @param borrowBody
     * @throws com.globallogic.bookshelf.exeptions.BookshelfResourceNotFoundException exception informing that book was not borrowed
     */
    @Transactional
    public void returnBook(Borrow borrowBody) {

        Integer id = borrowBody.getId();
        Borrow borrow = borrowRepository.findById(id).get();
        Book book = bookRepository.findById(borrow.getId()).get();

        if (!book.isAvailable()) {
            book.setAvailable(true);
            bookRepository.save(book);
            borrow.setReturned(new Date());
            borrowRepository.save(borrow);
        } else {
            throw new BookshelfResourceNotFoundException(String.format("Book with name : %s is not borrowed.", book.getName()));
        }
    }

    /**
     * Delete a specific, finished borrow.
     *
     * @param id id of the specific borrow
     * @throws BookshelfResourceNotFoundException exception informing that borrow doesn't exist
     * @throws BookshelfConflictException exception informing that borrow is still active
     */
    @Transactional
    public void deleteBorrow(Integer id) {
        Optional<Borrow> foundBorrow = borrowRepository.findById(id);
        if (foundBorrow.isEmpty()) {
            throw new BookshelfResourceNotFoundException(String.format("Borrow with id=%d doesn't exist",id));
        } else {
            Borrow borrow = foundBorrow.get();
            if (borrow.getReturned() == null) {
                throw new BookshelfConflictException(String.format("Borrow with id=%d is still active. Can't delete",id));
            } else {
                borrowRepository.deleteById(id);
            }
        }
    }

    /**
     * Get a borrow history and actual borrowed books info of the specific user based by firstname and surname
     *
     * @param firstname String represents firstname of the user
     * @param surname String represents surname of the user
     * @return List with every finished borrow of the user and active borrowed books with the number of them.
     */
    public List<Object> getUserBorrowHistory(String firstname, String surname) {
        List<Object> userBorrowHistory = new ArrayList<>();
        StringBuilder activeBooksInfo = new StringBuilder("Active borrow on books : ");
        int numberOfBorrowedBooks = 0;
        List<Borrow> foundBorrows = borrowRepository.findBorrowsByFirstnameAndSurname(firstname, surname);
        for (Borrow borrow : foundBorrows) {
            Book book = borrow.getBook();
            if (!book.isAvailable() && borrow.getReturned() == null) {
                activeBooksInfo.append("'").append(book.getName()).append("' ");
                numberOfBorrowedBooks += 1;
            }
        }
        foundBorrows.removeIf(
                borrow -> (borrow.getReturned() == null)
        );
        userBorrowHistory.add(foundBorrows);
        userBorrowHistory.add(activeBooksInfo.toString());
        userBorrowHistory.add(String.format("Number of active borrowed books = %d", numberOfBorrowedBooks));
        return userBorrowHistory;
    }
}