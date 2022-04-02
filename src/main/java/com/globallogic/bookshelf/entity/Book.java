package com.globallogic.bookshelf.entity;

import lombok.Data;

import javax.persistence.*;


/**
 * Definition class of the Book entity.
 *
 * @author Bartlomiej Chojnacki
 */
@Data
@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;
    protected String author;
    protected String name;

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    protected Category category;
}
