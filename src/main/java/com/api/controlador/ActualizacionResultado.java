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
    public List<ProductoNoEncontrado> noEncontrados; // ✅ Cambio aquí
    public List<String> precioMenor;
    
    
 // ✅ Agrega static
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductoNoEncontrado {
        public String textoOriginal;
        public String nombre;
        public String precio;
    }
}


