package com.springmememuseumrest.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "memes")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Meme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Column(nullable = false)
    private String imageUrl;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    // Relazione con Utente (autore del meme)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // Relazione con Comment
    @OneToMany(mappedBy = "meme", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    // Relazione con Vote
    @OneToMany(mappedBy = "meme", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vote> votes;

    // Relazione con Tag
    @ManyToMany
    @JoinTable(
        name = "meme_tags",
        joinColumns = @JoinColumn(name = "meme_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;

    @Column
    private LocalDate lastUsedDate;  
}