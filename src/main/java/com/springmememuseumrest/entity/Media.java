package com.springmememuseumrest.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "media", uniqueConstraints = @UniqueConstraint(columnNames = "date"))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String url;

    @NotBlank
    @Column(nullable = false)
    private String type;
    
    @Column(nullable = false)
    private Long size; 
}
