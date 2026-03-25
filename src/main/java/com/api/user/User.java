package com.api.user;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users", schema = "", catalog = "")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "firebaseUid", unique = true, nullable = false)
	private String firebaseUid;

	@Column(nullable = false)
	private String email;

	@Column(name = "tenantId",nullable = false)
	private Long tenantId; // proveedor
	
	@Column(name = "nombre")
	private String nombre;

	@Column(name = "edad")
	private Integer edad;

	@Column(name = "rubro")
	private String rubro;
	
	@Column(name = "push_token")
	private String pushToken;
	
	@Column(name = "frecuencia_deudores")
	private String frecuenciaDeudores;
	
	@ManyToOne
	@JoinColumn(name = "plan_id")
	private Plan plan;
	
	@Column(name ="trial_expira")
	private LocalDateTime trialExpira;
}
