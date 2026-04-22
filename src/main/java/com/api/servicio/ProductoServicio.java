package com.api.servicio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.controlador.ActualizacionResultado;
import com.api.controlador.ActualizacionResultado.ProductoNoEncontrado;
import com.api.entidad.productos;
import com.api.repositorio.repoProducto;
import com.api.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@Service
public class ProductoServicio {

	@Autowired
	private repoProducto productoRepository;
	@Autowired
	private UserRepository userRepository;

	// Helper para obtener tenantId del request
	private Long getTenantId(HttpServletRequest request) {
		String firebaseUid = (String) request.getAttribute("firebaseUid");
		return userRepository.findByFirebaseUid(firebaseUid)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado")).getTenantId();
	}

	public List<productos> getAll(HttpServletRequest request) {
		return productoRepository.findAllByTenantId(getTenantId(request));
	}

	public productos create(HttpServletRequest request, productos producto) {
		producto.setTenantId(getTenantId(request));
		return productoRepository.save(producto);
	}

	public productos update(HttpServletRequest request, String nombre, productos datos) {
		Long tenantId = getTenantId(request);
		productos producto = productoRepository.findByNombreProductoAndTenantId(nombre, tenantId)
				.orElseThrow(() -> new RuntimeException("Producto no encontrado"));

		producto.setPrecioActual(datos.getPrecioActual());
		return productoRepository.save(producto);
	}

//	Metodo para actualizacion colectiva
	public ActualizacionResultado actualizacionMasiva(HttpServletRequest request, String texto) {
	    Long tenantId = getTenantId(request);
//	    Obtiene productos de la BD
	    List<productos> lista = productoRepository.findAllByTenantId(tenantId);

	    ActualizacionResultado res = new ActualizacionResultado();
	    res.actualizados = new ArrayList<>();
	    res.noEncontrados = new ArrayList<>();
	    res.precioMenor = new ArrayList<>();

//	    Divide el texto en líneas  
//	    Cada línea es algo como "coca mini lata 6x220 $4600".
	    List<String> marcasGlobales = new ArrayList<>();
	    String[] lineas = texto.split("\\r?\\n");

	    for (String linea : lineas) {
	        linea = linea.trim().toLowerCase();

	        // 🔴 ignorar líneas basura
	        if (linea.isEmpty() || linea.startsWith("*") || !linea.contains("$"))
	            continue;

	        // 🔵 detectar regla global
	        if (linea.contains("aplican todos iguales")) {
	            marcasGlobales = List.of("coca", "coca cola", "fanta", "sprite", "schweppes");
	            continue;
	        }

	        try {
	            // separar nombre y precio
	            int indexPrecio = linea.lastIndexOf("$");

	            String nombre = linea.substring(0, indexPrecio)
	                .replaceAll("\\.", "")
	                .replaceAll("cc", "")
	                .replaceAll("litros?", "l")
	                .replaceAll("\\s+", " ")
	                .trim();

	            String precioStr = linea.substring(indexPrecio + 1)
	                .replaceAll("\\.", "")
	                .replace(",", "")
	                .trim();

	            BigDecimal precioNuevo = new BigDecimal(precioStr);

	            // 🔥 extraer datos y/o atributos 
	            String inputNormalizado = normalizar(nombre);
	            String marca = extraerMarca(inputNormalizado);
	            String capacidad = extraerCapacidadPrincipal(inputNormalizado);
	            String pack = extraerPack(inputNormalizado);
	            String formato = extraerFormato(inputNormalizado);


	            // 🔥 si hay regla global, usar todas las marcas
	            List<String> marcasAUsar = new ArrayList<>();

	            if (!marcasGlobales.isEmpty() && marca != null && marca.equals("coca")) {
	                marcasAUsar.addAll(marcasGlobales);
	            } else if (marca != null) {
	                marcasAUsar.add(marca);
	            } else {
	                res.noEncontrados.add(new ProductoNoEncontrado(linea, nombre, precioStr));
	                continue;
	            }
	            
//	            Busca coincidencias en la lista  
	            boolean encontrado = false;
	            boolean actualizado = false;

	            for (String m : marcasAUsar) {
	                for (productos p : lista) {

	                    String nombreBD = normalizar(p.getNombreProducto());
	                    String marcaNorm = normalizar(m);

	                    boolean coincide =
	                        nombreBD.contains(marcaNorm) &&
	                        (capacidad == null || nombreBD.contains(capacidad)) &&
	                        (pack == null || nombreBD.contains(pack)) &&
	                        (formato == null || nombreBD.contains(formato));

	                    if (coincide) {
	                        encontrado = true;

	                        BigDecimal actual = p.getPrecioActual() == null ? BigDecimal.ZERO : p.getPrecioActual();

	                        if (precioNuevo.compareTo(actual) < 0) {
	                            // ⚠ Encontrado pero precio menor
	                            res.precioMenor.add(p.getNombreProducto());
	                            continue;
	                        }

	                        // ✔ Actualizar
	                        p.setPrecioActual(precioNuevo);
	                        productoRepository.save(p);

	                        res.actualizados.add(p.getNombreProducto());
	                        actualizado = true;
	                    }
	                }
	            }

	            // 🔥 DECISIÓN FINAL
	            if (!encontrado) {
	                // ❌ Nunca encontró coincidencia
	                res.noEncontrados.add(new ProductoNoEncontrado(linea, nombre, precioStr));
	            }
//	            Si no se actualizó ninguno  
	        } catch (Exception e) {
	            res.noEncontrados.add(new ProductoNoEncontrado(linea, linea, "0"));
	            e.printStackTrace();
	        }
	    }

	    return res;
	}

	// ✅ NUEVA clase interna para retornar match con score
	private static class ProductoMatch {
	    productos producto;
	    int score;
	    
	    ProductoMatch(productos producto, int score) {
	        this.producto = producto;
	        this.score = score;
	    }
	}

	// ✅ MÉTODO MEJORADO: Devuelve match con score y valida mejor
	private ProductoMatch buscarProductoConScore(List<productos> lista, String inputNormalizado) {
	    productos mejorMatch = null;
	    int mejorScore = 0;
	    
	    for (productos p : lista) {
	        String nombreBD = p.getNombreProducto();
	        int score = calcularScore(inputNormalizado, nombreBD);
	        
	        if (score > mejorScore) {
	            mejorScore = score;
	            mejorMatch = p;
	        }
	    }
	    
	    String marca = extraerMarca(inputNormalizado);
	    int thresholdRequerido = (marca != null) ? 45 : 90;
	    
	    if (mejorScore < thresholdRequerido) {
	        return null; // No hay match suficientemente bueno
	    }
	    
	    return new ProductoMatch(mejorMatch, mejorScore);
	}

	private int calcularScore(String input, String nombreBD) {
	    int score = 0;
	    
	    // 1. Marca (30 puntos)
	    String marcaInput = extraerMarca(input);
	    String marcaBD = extraerMarca(nombreBD);
	    
	    // ✅ CAMBIO: Solo sumar puntos si AMBOS tienen marca Y coinciden
	    if (marcaInput != null && marcaBD != null && marcaInput.equals(marcaBD)) {
	        score += 30;
	    }
	    
	    // 2. Capacidad (40 puntos)
	    String capInput = extraerCapacidadPrincipal(input);
	    String capBD = extraerCapacidadPrincipal(nombreBD);
	    
	    if (capInput != null && capBD != null && capInput.equals(capBD)) {
	        score += 40;
	    }
	    
	    // 3. Formato (30 puntos)
	    String formatoInput = extraerFormato(input);
	    String formatoBD = extraerFormato(nombreBD);
	    
	 // 4. Bonus por match de palabra clave única (20 puntos)
	    if (formatoInput != null && formatoInput.equals("tetra") && 
	        formatoBD != null && formatoBD.equals("tetra") &&
	        !input.contains("ades")) { // Si NO es Ades
	        score += 20; // Boost para Cepita
	    }
	    
	    if (formatoInput != null && formatoBD != null && formatoInput.equals(formatoBD)) {
	        score += 30;
	    }
	    
	    return score;
	}
	
	private String extraerPack(String texto) {
	    texto = texto.replaceAll("\\s+", "");
	    
	    if (texto.matches(".*12x.*")) return "12x";
	    if (texto.matches(".*9x.*")) return "9x";
	    if (texto.matches(".*8x.*")) return "8x";
	    if (texto.matches(".*6x.*")) return "6x";
	    if (texto.matches(".*4x.*")) return "4x";
	    if (texto.matches(".*\\d+x.*")) {
	        return texto.replaceAll(".*?(\\d+x).*", "$1");
	    }
	    
	    return null;
	}

	private String extraerMarca(String texto) {
	    texto = texto.toLowerCase();
	    if (texto.contains("coca cola") || texto.contains("cocacola") || texto.contains("coca")) return "coca";
	    if (texto.contains("fanta")) return "fanta";
	    if (texto.contains("sprite")) return "sprite";
	    if (texto.contains("schweppes")) return "schweppes";
	    if (texto.contains("cepita")) return "cepita";
	    if (texto.contains("ades")) return "ades";
	    if (texto.contains("powerade")) return "powerade";
	    if (texto.contains("monster")) return "monster";
	    if (texto.contains("aquarius")) return "aquarius";
	    if (texto.contains("ivess")) return "ivess";
	    if (texto.contains("estambul")) return "estambul";
	    return null;
	}

	private String extraerCapacidadPrincipal(String textoOriginal) {
	    String texto = textoOriginal.toLowerCase()
	        .replaceAll("\\s+", "")
	        .replaceAll("\\.", ""); // ✅ AGREGA ESTO: quita puntos
	    
	    if (texto.matches(".*2500.*")) return "2500";
	    if (texto.matches(".*2250.*")) return "2250";
	    if (texto.matches(".*(2000|2l(?!itro)|2litros?|2 l|2l).*")) return "2000";
	    if (texto.matches(".*1750.*")) return "1750";
	    if (texto.matches(".*(1500|1\\.5l).*")) return "1500";
	    if (texto.matches(".*(1250).*")) return "1250";
	    if (texto.matches(".*(1000|1l|1lt|1litro|1litros|91l|1 l|1l).*")) return "1000";
	    if (texto.matches(".*995.*")) return "995";
	    if (texto.matches(".*500(?!0).*")) return "500";
	    if (texto.matches(".*473.*")) return "473";
	    if (texto.matches(".*354.*")) return "354";
	    if (texto.matches(".*300.*")) return "300";
	    if (texto.matches(".*237.*")) return "237";
	    if (texto.matches(".*220.*")) return "220";
	    if (texto.matches(".*200(?!0).*")) return "200";
	    if (texto.matches(".*6\\.?5.*")) return "6500";
	    
	    return null;
	}

	private String extraerFormato(String texto) {
	    texto = texto.toLowerCase();
	    if (texto.contains("mini")) return "mini";
	    if (texto.contains("Mini")) return "mini";
	    if (texto.contains("lata")) return "lata";
	    if (texto.contains("Lata")) return "lata";
	    if (texto.contains("vidrio")) return "vidrio";
	    if (texto.contains("Vidrio")) return "vidrio";
	    if (texto.contains("cajon") || texto.contains("cajón")) return "cajon";
	    if (texto.contains("tetra") || texto.contains("brick")) return "tetra";
	    if (texto.contains("botella") || texto.contains("botellita")) return "botella";
	    if (texto.contains("sifon") || texto.contains("sifón")) return "sifon";
	    if (texto.contains("bidon") || texto.contains("bidón")) return "bidon";
	    // ✅ CAMBIO: Detectar "plástico/plastica" como formato
	    if (texto.contains("plastic")) return "plastico";
	    return null;
	}

	public String normalizar(String texto) {
	    return texto.toLowerCase()
	    	.replaceAll("\\.", "")
	        .replaceAll("\\s+", "")
	        .replace("cocacola", "coca")
	        .replace("coca cola", "coca")
	        .replace("plástico", "plastico")
	        .replace("cajón", "cajon")
	        .replace("bidon", "bidón")
	        .replace("sifon", "sifón")
	        .replace("litros", "l")
	        .replace("litro", "l")
	        .replace("ml", "")
	        .replace("cc", "")
	        .replace("x", "x")
	        .replace(".", "")
	        .replace(",", "")
	        .replaceAll("[áàäâ]", "a")
	        .replaceAll("[éèëê]", "e")
	        .replaceAll("[íìïî]", "i")
	        .replaceAll("[óòöô]", "o")
	        .replaceAll("[úùüû]", "u")
	        .replaceAll("[^a-z0-9]", "");
	}
	
//	Metodo para borrar un producto
	@Transactional
	public void delete(HttpServletRequest request, String nombre) {
		Long tenantId = getTenantId(request);
		productoRepository.findByNombreProductoAndTenantId(nombre, tenantId)
				.orElseThrow(() -> new RuntimeException("Producto no encontrado"));
		productoRepository.deleteByNombreProductoAndTenantId(nombre, tenantId);
	}
}
