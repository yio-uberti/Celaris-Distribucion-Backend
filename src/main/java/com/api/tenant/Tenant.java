package com.api.tenant;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tenant", schema = "", catalog = "")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
    private String tipo; // AUTONOMO / EMPRESA

    @Column(name = "nombre_fantasia")
    private String nombreFantasia;

    @Column(name = "razon_social")
    private String razonSocial;

    private String cuit;
    private String telefono;
    private String email;
    private String nombre;

    @Column(name = "fecha_alta")
    private LocalDateTime fechaAlta;

    private Boolean activo;


}
