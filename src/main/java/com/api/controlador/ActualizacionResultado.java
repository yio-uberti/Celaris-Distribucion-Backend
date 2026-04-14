package com.api.controlador;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizacionResultado {

	public List<String> actualizados;
    public List<String> noEncontrados;
    public List<String> precioMenor;
}
