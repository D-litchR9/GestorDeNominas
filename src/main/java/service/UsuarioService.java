package service;

import model.Usuario;
import util.EntityManagerUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.hibernate.service.spi.ServiceException;

@ApplicationScoped
public class UsuarioService {

    @PostConstruct
    void init() {
        // Forzar la creación de la fábrica al iniciar el contexto CDI
        EntityManagerUtil.getEntityManager().close(); // solo para inicializar la fábrica estática
    }

    @PreDestroy
    void destroy() {
        EntityManagerUtil.close();
    }

    // Método plantilla para operaciones transaccionales con retorno
    private <T> T ejecutarTransaccion(Function<EntityManager, T> action) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {
            em.getTransaction().begin();
            T resultado = action.apply(em);
            em.getTransaction().commit();
            return resultado;
        } catch (Exception e) {
            throw new ServiceException("Operación fallida", e);
        }
    }

    // Plantilla para operaciones transaccionales sin retorno
    private void ejecutarTransaccion(Consumer<EntityManager> action) {
        ejecutarTransaccion(em -> {
            action.accept(em);
            return null; // necesario para ajustar la firma
        });
    }

    public Usuario guardar(Usuario usuario) {
        return ejecutarTransaccion(em -> {
            usuario.calcularConceptos();
            em.persist(usuario);
            return usuario;
        });
    }

    public Usuario actualizar(Usuario usuario) {
        return ejecutarTransaccion(em -> {
            usuario.calcularConceptos();
            return em.merge(usuario);
        });
    }

    public void eliminar(Long id) {
        ejecutarTransaccion((Consumer<EntityManager>) em -> {
            Usuario usuario = em.find(Usuario.class, id);
            if (usuario == null) {
                throw new ServiceException("No existe el usuario con ID: " + id);
            }
            em.remove(usuario);
        });
    }

    public List<Usuario> listarTodos() {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {
            TypedQuery<Usuario> query = em.createQuery(
                "SELECT u FROM Usuario u ORDER BY u.id", Usuario.class);
            return query.getResultList();
        } catch (Exception e) {
            throw new ServiceException("Error al listar usuarios", e);
        }
    }

    public Usuario buscarPorId(Long id) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {
            return em.find(Usuario.class, id);
        } catch (Exception e) {
            throw new ServiceException("Error al buscar usuario con ID " + id, e);
        }
    }
}