package com.api.entidad;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.api.user.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "venta", schema = "", catalog = "")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ventas {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_venta")
	private Integer idVenta;
	
	@ManyToOne
	@JoinColumn(name = "id_cliente")
	private clientes cliente;
	@Column
	private BigDecimal total;
	@Column
	private LocalDateTime fecha_hora;
	@Column(name ="tenant_id")
	private Long tenantId;
	@Column
	private String estado;
	@Column(name = "fecha_entrega")
	private LocalDate fechaEntrega;
	
	 // Lista de detalles y pagos — JPA los carga
	@OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<DetalleVenta> detalles;
	
	@OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference("venta-pagos")
	private List<VentaTipoPago> pagos;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "registrado_por")
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "tenant", "plan", "suscripciones"})
	private User registradoPor;
	
	@Column
	private Integer descuento;
	
}
