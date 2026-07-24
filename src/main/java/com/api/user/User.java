package com.api.user;

import java.math.BigInteger;
import java.time.LocalDateTime;

import com.api.tenant.Tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

	@Column(name = "firebase_uid", unique = true, nullable = false)
	private String firebaseUid;

	@Column(nullable = false)
	private String email;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tenant_id")
	private Tenant tenant;

	@Column(name = "nombre")
	private String nombre;
	@Column(name = "apellido")
	private String apellido;
	@Column(name = "edad")
	private Integer edad;
	@Column(name = "dni")
	private BigInteger dni;

	@Column(name = "password_hash")
	private String passwordHash;

	@Column(name = "rubro")
	private String rubro;

	@Column(name = "push_token")
	private String pushToken;

	@Column(name = "frecuencia_deudores")
	private String frecuenciaDeudores;

//	@ManyToOne
//	@JoinColumn(name = "plan_id")
//	private Plan plan;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "rol_id")
	private Rol rol;

	@Column(name = "activo")
	private Boolean activo;

	@Column(name = "fecha_alta")
	private LocalDateTime fechaAlta;

	@Column(name = "fecha_registro")
	private LocalDateTime fechaRegistro;

	@Column(name = "encuesta_completada")
	private Boolean encuestaCompletada = false;

}
