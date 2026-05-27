import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public class testConexion {
    public static void main(String[] args) {
        Map<String, Object> props = new HashMap<>();
        props.put("jakarta.persistence.transactionType", "RESOURCE_LOCAL");

        // Opcional: puedes pasar propiedades adicionales si quieres sobrescribir
        // props.put("hibernate.hikari.dataSource.password", "admin");

        try (EntityManagerFactory emf = Persistence.createEntityManagerFactory("NominaPU", props);
             EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();
            Object result = em.createNativeQuery("SELECT 1").getSingleResult();
            System.out.println("✅ Conexión exitosa. Resultado: " + result);
            em.getTransaction().commit();

        } catch (Exception e) {
            System.err.println("❌ Error al conectar: " + e.getMessage());
            e.printStackTrace();
        }
    }
}