package com.globallogic.bookshelf.utils;

import com.globallogic.bookshelf.entity.Book;
import com.globallogic.bookshelf.entity.Category;
import com.globallogic.bookshelf.exeptions.BookshelfResourceNotFoundException;
import com.globallogic.bookshelf.repository.CategoryRepository;

import java.util.Date;

/**
 * Special class for verifying specific data provided to a specific method.
 */
public class Verification {

    /**
     * Method that's handle category setup for certain book entity (sets id based on the category object)
     *
     * @param category object of the category
     * @param book object of the book
     */
    public static void ofTheCategory(Category category, Book book, CategoryRepository categoryRepository) {
        if (category == null) {
            throw new BookshelfResourceNotFoundException("Category not found");
        } else if (category.getName().equals("Default")) {
            book.setCategory(new Category(4, "Default"));
        } else {
            book.setCategory(categoryRepository.getById(category.getId()));
        }
    }

    /**
     * Method that's handle date setup. Returns date object initialized with current date or saved param value.
     *
     * @param date date to be verified
     * @return date value (current or given as param)
     */
    public static Date ofTheDate(Date date) {
        if (date == null) {
            date = new Date();
        }
        return date;
    }

}
