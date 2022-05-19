package com.globallogic.bookshelf.service;

import com.globallogic.bookshelf.entity.Book;
import com.globallogic.bookshelf.entity.Borrow;
import com.globallogic.bookshelf.entity.Category;
import com.globallogic.bookshelf.exeptions.BookshelfConflictException;
import com.globallogic.bookshelf.exeptions.BookshelfResourceNotFoundException;
import com.globallogic.bookshelf.repository.BookRepository;
import com.globallogic.bookshelf.repository.BorrowRepository;
import com.globallogic.bookshelf.repository.CategoryRepository;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {BookShelfService.class})
@ExtendWith(MockitoExtension.class)
public class BookShelfServiceTest {

    private static Book bookAvailable, bookNotAvailable;
    private static Borrow borrow;
    private static Borrow borrow1;
    private static Category category;
    private static HashMap<Book, String> booksWithNewestBorrow;
    private static HashMap<Book, String> booksAvailability;
    private static HashMap<Book, String> booksAllAvailability;
    private static HashMap<String, String> books;
    private static String author, title, firstname, lastname;
    private static final int id = 1;

    @Mock
    private static BookRepository bookRepository;

    @Mock
    private static BorrowRepository borrowRepository;

    @Mock
    private static CategoryRepository categoryRepository;

    @InjectMocks
    private static BookShelfService bookShelfService;


    private static List<Book> allBooks;
    private static ArrayList<Object> booksWithNewestBorrowSort;

    @BeforeAll
    public static void initVariables() {
        author = "Adam Mickiewicz";
        title = "Dziady";
        category = new Category(4, "Default");

        bookShelfService = new BookShelfService(bookRepository, borrowRepository, categoryRepository);
        bookAvailable = new Book(
                null, author, title, true, category);
        bookNotAvailable = new Book(
                2, author, title, false,
                new Category(2, "categoryName"));

        allBooks = new ArrayList<>();
        allBooks.add(bookAvailable);
        allBooks.add(bookNotAvailable);

        firstname = "Andrzej";
        lastname = "Kowalski";

        Date date = new Date();
        borrow = new Borrow(
                1, date, date, firstname, lastname,
                "random comment",
                bookNotAvailable);
        borrow1 = new Borrow(
                1, date, null, firstname, lastname,
                null,
                bookAvailable);

        books = new HashMap<>();
        books.put(bookAvailable.getAuthor(), bookAvailable.getTitle());
        books.put(bookNotAvailable.getAuthor(), bookNotAvailable.getTitle());

        booksWithNewestBorrowSort = new ArrayList<>();
        booksWithNewestBorrowSort.add(borrow1);


        booksWithNewestBorrow = new HashMap<>();
        booksWithNewestBorrow.put(bookNotAvailable,
                String.format("Name : %s %s : Date of borrowing book %s",
                        borrow.getFirstname(),
                        borrow.getLastname(),
                        borrow.getBorrowed()));
        booksAllAvailability = new HashMap<>();
        booksAllAvailability.put(bookAvailable, "Available");
        booksAvailability = new HashMap<>();
        booksAvailability.put(bookAvailable, "available");
        booksAvailability.put(bookNotAvailable,
                String.format("%s %s : %s",
                        borrow.getFirstname(),
                        borrow.getLastname(),
                        borrow.getBorrowed()));
    }

    @Test
    public void createSuccessTest() {
        Mockito.doReturn(category).when(categoryRepository).findByName(bookAvailable.getCategory().getName());

        bookShelfService.create(title, author, true, category.getName());

        Mockito.verify(bookRepository).save(bookAvailable);
    }

    @Test
    public void deleteBookSuccessTest() {
        Mockito.doReturn(Optional.of(bookAvailable)).when(bookRepository).findById(id);
        Mockito.doReturn(List.of(borrow)).when(borrowRepository).findAllByBook(bookAvailable);

        bookShelfService.delete(id);
        Mockito.verify(borrowRepository).delete(borrow);
        Mockito.verify(bookRepository).deleteById(id);
    }

    @Test
    public void deleteBookResourceNotFoundExceptionTest() {
        Mockito.doReturn(Optional.empty()).when(bookRepository).findById(id);

        Exception exception = assertThrows(BookshelfResourceNotFoundException.class, () ->
                bookShelfService.delete(id)
        );

        String expectedMessage = String.format("Book with id=%d doesn't exist", id);
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void deleteBookConflictExceptionTest() {
        Mockito.doReturn(Optional.of(bookNotAvailable)).when(bookRepository).findById(id);

        Exception exception = assertThrows(BookshelfConflictException.class, () ->
                bookShelfService.delete(id)
        );

        String expectedMessage = String.format("Book with id=%d is still borrowed. Can't delete", id);
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void getAllBooksTest() {
        Mockito.doReturn(allBooks).when(bookRepository).findAll();

        HashMap<String, String> booksAvailabilityReturn = bookShelfService.getAllBooks();

        assertEquals(books, booksAvailabilityReturn);
    }

    @Test
    public void getAllBooksAvailableTest() {
        ArrayList<Book> bookArrayList = new ArrayList<>();
        bookArrayList.add(bookAvailable);

        Mockito.doReturn(bookArrayList).when(bookRepository).findAll();
        HashMap<Book, String> booksAvailabilityReturn = bookShelfService.getAllBooksAvailable();

        assertEquals(booksAllAvailability, booksAvailabilityReturn);
    }

//    @Test
//    public void getListOfBorrowedBooksSortTest() {
//
//        ArrayList<Borrow> bookBorrows = new ArrayList<>();
//        bookBorrows.add(borrow1);
//
//        Mockito.doReturn(allBooks).when(bookRepository).findAll();
//        Mockito.doReturn(bookBorrows).when(borrowRepository).findAllByBook(bookNotAvailable);
//
//        List<Borrow> booksAvailabilityReturn = bookShelfService.getListOfBorrowedBooksSort();
//
//        assertEquals(booksAvailabilityReturn, booksWithNewestBorrowSort);
//    }


    /**
     * Method under test: {@link BookShelfService#getListOfBorrowedBooks()}
     */
    @Test
    void testGetListOfBorrowedBooks() {
        when(this.bookRepository.findAll()).thenReturn(new ArrayList<>());
        assertTrue(this.bookShelfService.getListOfBorrowedBooks().isEmpty());
        verify(this.bookRepository).findAll();
    }

    /**
     * Method under test: {@link BookShelfService#getListOfBorrowedBooks()}
     */
    @Test
    void GetListOfBorrowedBooksTest() {
        Category category = new Category();
        category.setId(1);
        category.setName("Name");

        Book book = new Book();
        book.setAuthor("JaneDoe");
        book.setAvailable(true);
        book.setCategory(category);
        book.setId(1);
        book.setTitle("Dr");

        ArrayList<Book> bookList = new ArrayList<>();
        bookList.add(book);
        when(this.bookRepository.findAll()).thenReturn(bookList);
        assertTrue(this.bookShelfService.getListOfBorrowedBooks().isEmpty());
        verify(this.bookRepository).findAll();
    }

    /**
     * Method under test: {@link BookShelfService#getListOfBorrowedBooks()}
     */
    @Test
    void testGetListOfBorrowedBooks3() {
        when(this.bookRepository.findAll()).thenThrow(new BookshelfResourceNotFoundException("An error occurred"));
        assertThrows(BookshelfResourceNotFoundException.class, () -> this.bookShelfService.getListOfBorrowedBooks());
        verify(this.bookRepository).findAll();
    }

    /**
     * Method under test: {@link BookShelfService#getListOfBorrowedBooks()}
     */
    @Test
    void testGetListOfBorrowedBooks4() {
        when(this.borrowRepository.findAllByBook((Book) any())).thenReturn(new ArrayList<>());

        Category category = new Category();
        category.setId(1);
        category.setName("Name");
        Book book = mock(Book.class);
        when(book.isAvailable()).thenReturn(false);
        doNothing().when(book).setAuthor((String) any());
        doNothing().when(book).setAvailable(anyBoolean());
        doNothing().when(book).setCategory((Category) any());
        doNothing().when(book).setId((Integer) any());
        doNothing().when(book).setTitle((String) any());
        book.setAuthor("JaneDoe");
        book.setAvailable(true);
        book.setCategory(category);
        book.setId(1);
        book.setTitle("Dr");

        ArrayList<Book> bookList = new ArrayList<>();
        bookList.add(book);
        when(this.bookRepository.findAll()).thenReturn(bookList);
        assertTrue(this.bookShelfService.getListOfBorrowedBooks().isEmpty());
        verify(this.borrowRepository).findAllByBook((Book) any());
        verify(this.bookRepository).findAll();
        verify(book).isAvailable();
        verify(book).setAuthor((String) any());
        verify(book).setAvailable(anyBoolean());
        verify(book).setCategory((Category) any());
        verify(book).setId((Integer) any());
        verify(book).setTitle((String) any());
    }

    @Test
    public void getBooksAvailabilityTest() {
        ArrayList<Borrow> bookBorrows = new ArrayList<>();
        bookBorrows.add(borrow);

        Mockito.doReturn(allBooks).when(bookRepository).findAll();
        Mockito.doReturn(bookBorrows).when(borrowRepository).findAllByBook(bookNotAvailable);

        HashMap<Book, String> booksAvailabilityReturn = bookShelfService.getBooksAvailability();

        assertEquals(booksAvailability, booksAvailabilityReturn);
    }

    @Test
    public void getBookHistoryTest() {
        ArrayList<Borrow> bookBorrows = new ArrayList<>();
        bookBorrows.add(borrow);
        List<String> borrowInfo = new ArrayList<>();

        borrowInfo.add("Name: " + borrow.getFirstname() + " " + borrow.getLastname());
        borrowInfo.add("Date of borrowing book: " + borrow.getBorrowed().toString());
        borrowInfo.add("Date of return book: " + borrow.getReturned().toString());
        borrowInfo.add("Comment: " + borrow.getComment());
        borrowInfo.add("Book is available");

        HashMap<Book, List<String>> bookListHashMap = new HashMap<>();
        bookListHashMap.put(bookAvailable, borrowInfo);
        Mockito.doReturn(bookAvailable).when(bookRepository).findByTitle(bookAvailable.getTitle());
        Mockito.doReturn(bookBorrows).when(borrowRepository).findAllByBook(bookAvailable);

        HashMap<Book, List<String>> bookHistory = bookShelfService.getBooksHistory(bookAvailable.getTitle());

        assertEquals(bookListHashMap, bookHistory);
    }

    @Test
    public void getBookHistoryWithNoCommentAndNotReturnTest() {
        ArrayList<Borrow> bookBorrows = new ArrayList<>();
        bookBorrows.add(borrow1);
        List<String> borrowInfo = new ArrayList<>();
        borrowInfo.add("Name: " + borrow1.getFirstname() + " " + borrow1.getLastname());
        borrowInfo.add("Date of borrowing book: " + borrow1.getBorrowed().toString());
        borrowInfo.add("Book not returned");
        borrowInfo.add("No comment");
        borrowInfo.add("Book is not available");

        HashMap<Book, List<String>> bookListHashMap = new HashMap<>();
        bookListHashMap.put(bookNotAvailable, borrowInfo);
        Mockito.doReturn(bookNotAvailable).when(bookRepository).findByTitle(bookNotAvailable.getTitle());
        Mockito.doReturn(bookBorrows).when(borrowRepository).findAllByBook(bookNotAvailable);

        HashMap<Book, List<String>> bookHistory = bookShelfService.getBooksHistory(bookNotAvailable.getTitle());

        assertEquals(bookListHashMap, bookHistory);
    }

//
//    @Test
//    public void createBookshelfResourceNotFoundExceptionTest() {
//        Exception exception = assertThrows(BookshelfResourceNotFoundException.class, () ->
//                bookShelfService.create(bookNotAvailable)
//        );
//
//        String expectedMessage = "Category not found";
//        String actualMessage = exception.getMessage();
//        assertTrue(actualMessage.contains(expectedMessage));
//    }
}
