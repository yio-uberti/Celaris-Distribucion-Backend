package com.api.user;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

@Entity
@Table(name = "emails_eliminados", schema = "", catalog = "")
@Data
@Builder
public class Emails_eliminados {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@Column
	private String email;
	
	@Column(name = "fecha_eliminacion", columnDefinition = "TIMESTAMP DEFAULT NOW()")
    private LocalDateTime fechaEliminacion;
}
