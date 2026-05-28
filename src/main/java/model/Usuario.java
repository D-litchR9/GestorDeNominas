package model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "dias_trabajados", nullable = false)
    private int diasTrabajados;  // primitivo para evitar null

    @Column(name = "salario_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal salarioBase;

    // Campos calculados (solo modificables desde calcularConceptos())
    @Column(name = "valor_pension", precision = 12, scale = 2)
    private BigDecimal valorPension;

    @Column(name = "valor_salud", precision = 12, scale = 2)
    private BigDecimal valorSalud;

    @Column(name = "auxilio_transporte", precision = 12, scale = 2)
    private BigDecimal auxilioTransporte;

    @Column(name = "salario_final", precision = 12, scale = 2)
    private BigDecimal salarioFinal;

    // Constantes para normativa colombiana 2025
    public static final BigDecimal SALARIO_MINIMO_2025 = new BigDecimal("1750905.00");
    public static final BigDecimal AUXILIO_TRANSPORTE_2025 = new BigDecimal("250000.00");
    public static final BigDecimal PORCENTAJE_SALUD = new BigDecimal("0.04");   // 4% empleado
    public static final BigDecimal PORCENTAJE_PENSION = new BigDecimal("0.04"); // 4% empleado
    private static final BigDecimal DIAS_MES = new BigDecimal("30");

    public Usuario() {
        this.salarioBase = BigDecimal.ZERO;
        this.valorPension = BigDecimal.ZERO;
        this.valorSalud = BigDecimal.ZERO;
        this.auxilioTransporte = BigDecimal.ZERO;
        this.salarioFinal = BigDecimal.ZERO;
    }

    // Getters públicos

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

    public int getDiasTrabajados() {
        return diasTrabajados;
    }

    public void setDiasTrabajados(int diasTrabajados) {
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

    public BigDecimal getValorSalud() {
        return valorSalud;
    }

    public BigDecimal getAuxilioTransporte() {
        return auxilioTransporte;
    }

    public BigDecimal getSalarioFinal() {
        return salarioFinal;
    }

    // Setters privados para campos calculados (solo uso interno)
    private void setValorPension(BigDecimal valorPension) {
        this.valorPension = valorPension;
    }

    private void setValorSalud(BigDecimal valorSalud) {
        this.valorSalud = valorSalud;
    }

    private void setAuxilioTransporte(BigDecimal auxilioTransporte) {
        this.auxilioTransporte = auxilioTransporte;
    }

    private void setSalarioFinal(BigDecimal salarioFinal) {
        this.salarioFinal = salarioFinal;
    }

    /**
     * Calcula todos los conceptos de nómina con los datos actuales.
     * Lanza IllegalArgumentException si los datos base no son válidos.
     */
    public void calcularConceptos() {
        // Validaciones previas
        if (diasTrabajados <= 0 || diasTrabajados > 31) {
            throw new IllegalArgumentException("Días trabajados deben estar entre 1 y 31");
        }
        Objects.requireNonNull(salarioBase, "El salario base no puede ser nulo");
        if (salarioBase.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El salario base debe ser mayor que cero");
        }

        // 1. Base diaria
        BigDecimal baseDiaria = salarioBase.divide(DIAS_MES, 4, RoundingMode.HALF_UP);

        // 2. Salario proporcional a los días trabajados
        BigDecimal diasDecimal = BigDecimal.valueOf(diasTrabajados);
        BigDecimal salarioProporcional = baseDiaria.multiply(diasDecimal)
                .setScale(2, RoundingMode.HALF_UP);

        // 3. Cálculo de salud (4%)
        BigDecimal salud = salarioProporcional.multiply(PORCENTAJE_SALUD)
                .setScale(2, RoundingMode.HALF_UP);

        // 4. Cálculo de pensión (4%)
        BigDecimal pension = salarioProporcional.multiply(PORCENTAJE_PENSION)
                .setScale(2, RoundingMode.HALF_UP);

        // 5. Auxilio de transporte (proporcional si corresponde)
        BigDecimal dosSMMLV = SALARIO_MINIMO_2025.multiply(new BigDecimal("2"));
        BigDecimal auxilio;
        if (salarioBase.compareTo(dosSMMLV) <= 0) {
            // Prorrateo: auxilio * días / 30
            auxilio = AUXILIO_TRANSPORTE_2025
                    .divide(DIAS_MES, 4, RoundingMode.HALF_UP)
                    .multiply(diasDecimal)
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            auxilio = BigDecimal.ZERO;
        }

        // 6. Salario final
        BigDecimal finalSalario = salarioProporcional
                .add(auxilio)
                .subtract(salud)
                .subtract(pension)
                .setScale(2, RoundingMode.HALF_UP);

        // Asignación segura
        setValorSalud(salud);
        setValorPension(pension);
        setAuxilioTransporte(auxilio);
        setSalarioFinal(finalSalario);
    }
}