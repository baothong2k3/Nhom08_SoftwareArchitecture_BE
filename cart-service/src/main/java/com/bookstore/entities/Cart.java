package com.bookstore.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
<<<<<<< HEAD
=======
import lombok.Builder;
>>>>>>> 70eb395 (create docker)
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
<<<<<<< HEAD
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cart")
=======
@Builder
@NoArgsConstructor
@AllArgsConstructor
>>>>>>> 70eb395 (create docker)
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
<<<<<<< HEAD

    @Column(nullable = false, name = "user_id")
    private Long userId;

    @Column(nullable = false, name = "book_id")
    private Long bookId;

    @Column(nullable = false, name = "quantity")
=======
    private Long userId;
    private Long bookId;
>>>>>>> 70eb395 (create docker)
    private int quantity;
}
