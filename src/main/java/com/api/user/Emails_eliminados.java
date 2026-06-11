package com.api.user;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "emails_eliminados", schema = "", catalog = "")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Emails_eliminados {
	
	@Id
    @Column(name = "email")
	private String email;
	
	@Column(name = "fecha_eliminacion", columnDefinition = "TIMESTAMP DEFAULT NOW()")
    private LocalDateTime fechaEliminacion;
}
