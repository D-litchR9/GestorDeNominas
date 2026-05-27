package model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "usuarios")
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String nombre;
    
    @Column(name = "dias_trabajados", nullable = false)
    private Integer diasTrabajados;
    
    @Column(name = "salario_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal salarioBase;
    
    // Campos calculados (se actualizan en los métodos de negocio)
    @Column(name = "valor_pension", precision = 12, scale = 2)
    private BigDecimal valorPension;
    
    @Column(name = "valor_salud", precision = 12, scale = 2)
    private BigDecimal valorSalud;
    
    @Column(name = "auxilio_transporte", precision = 12, scale = 2)
    private BigDecimal auxilioTransporte;
    
    @Column(name = "salario_final", precision = 12, scale = 2)
    private BigDecimal salarioFinal;

    // Constantes para la normativa colombiana 2025
    public static final BigDecimal SALARIO_MINIMO_2025 = new BigDecimal("1423500.00");//[reference:1]
    public static final BigDecimal AUXILIO_TRANSPORTE_2025 = new BigDecimal("200000.00");//[reference:2]
    public static final BigDecimal PORCENTAJE_SALUD = new BigDecimal("0.04"); // 4% empleado[reference:3]
    public static final BigDecimal PORCENTAJE_PENSION = new BigDecimal("0.04"); // 4% empleado[reference:4]
    
    public Usuario() {
        this.salarioBase = BigDecimal.ZERO;
        this.valorPension = BigDecimal.ZERO;
        this.valorSalud = BigDecimal.ZERO;
        this.auxilioTransporte = BigDecimal.ZERO;
        this.salarioFinal = BigDecimal.ZERO;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getDiasTrabajados() {
        return diasTrabajados;
    }

    public void setDiasTrabajados(Integer diasTrabajados) {
        this.diasTrabajados = diasTrabajados;
    }

    public BigDecimal getSalarioBase() {
        return salarioBase;
    }

    public void setSalarioBase(BigDecimal salarioBase) {
        this.salarioBase = salarioBase;
    }

    public BigDecimal getValorPension() {
        return valorPension;
    }

    public void setValorPension(BigDecimal valorPension) {
        this.valorPension = valorPension;
    }

    public BigDecimal getValorSalud() {
        return valorSalud;
    }

    public void setValorSalud(BigDecimal valorSalud) {
        this.valorSalud = valorSalud;
    }

    public BigDecimal getAuxilioTransporte() {
        return auxilioTransporte;
    }

    public void setAuxilioTransporte(BigDecimal auxilioTransporte) {
        this.auxilioTransporte = auxilioTransporte;
    }

    public BigDecimal getSalarioFinal() {
        return salarioFinal;
    }

    public void setSalarioFinal(BigDecimal salarioFinal) {
        this.salarioFinal = salarioFinal;
    }
    
    // Método para calcular todos los conceptos de nómina
    public void calcularConceptos() {
        // 1. Calcular base diaria
        BigDecimal baseDiaria = this.salarioBase.divide(new BigDecimal("30"), 2, RoundingMode.HALF_UP);
        // 2. Calcular salario proporcional a los días trabajados
        BigDecimal salarioProporcional = baseDiaria.multiply(new BigDecimal(this.diasTrabajados));
        
        // 3. Calcular salud (4% del salario proporcional)
        this.valorSalud = salarioProporcional.multiply(PORCENTAJE_SALUD).setScale(2, RoundingMode.HALF_UP);
        
        // 4. Calcular pensión (4% del salario proporcional)
        this.valorPension = salarioProporcional.multiply(PORCENTAJE_PENSION).setScale(2, RoundingMode.HALF_UP);
        
        // 5. Determinar si tiene derecho a auxilio de transporte
        // Se otorga si el salario base es <= 2 SMMLV (Salarios Mínimos Mensuales Legales Vigentes)
        BigDecimal dosSMMLV = SALARIO_MINIMO_2025.multiply(new BigDecimal("2"));
        if (this.salarioBase.compareTo(dosSMMLV) <= 0) {
            this.auxilioTransporte = AUXILIO_TRANSPORTE_2025;
        } else {
            this.auxilioTransporte = BigDecimal.ZERO;
        }
        
        // 6. Calcular salario final (salario proporcional + auxilio - deducciones)
        this.salarioFinal = salarioProporcional
                .add(this.auxilioTransporte)
                .subtract(this.valorSalud)
                .subtract(this.valorPension)
                .setScale(2, RoundingMode.HALF_UP);
    }
}