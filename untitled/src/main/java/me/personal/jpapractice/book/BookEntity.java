package me.personal.jpapractice.book;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "books")
@Table(name = "books")
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "title", nullable = false)
    String title;

    @Column(name = " isbn", unique = true, length = 13)
    String isbn;

    @Column(name = "price")
    BigDecimal price;

    @Column(name = "publishedDate")
    LocalDate publishedDate;

    @Column(name = "pageCount")
    Integer pageCount;
}