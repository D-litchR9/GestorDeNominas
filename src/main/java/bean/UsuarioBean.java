package bean;

import model.Usuario;
import service.UsuarioService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class UsuarioBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Inject
    private UsuarioService usuarioService;
    
    private List<Usuario> listaUsuarios;
    private Usuario usuarioSeleccionado;
    private Usuario nuevoUsuario;
    private String modoFormulario; // "crear" o "editar"
    
    @PostConstruct
    public void init() {
        cargarLista();
        nuevoUsuario = new Usuario();
        modoFormulario = "crear";	
    }
    
    public void cargarLista() {
        listaUsuarios = usuarioService.listarTodos();
    }
    
    public void prepararCrear() {
    	//usuarioSeleccionado = null;
        nuevoUsuario = new Usuario();
        modoFormulario = "crear";
    }
    
    public void prepararEditar(Usuario usuario) {
        this.usuarioSeleccionado = usuario;
        modoFormulario = "editar";
    }
    
    public void guardar() {
        try {
            usuarioService.guardar(nuevoUsuario);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Usuario creado correctamente"));
            cargarLista();
            prepararCrear();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }
    
    public void actualizar() {
        try {
            usuarioService.actualizar(usuarioSeleccionado);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Usuario actualizado correctamente"));
            cargarLista();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
        prepararCrear();
    }
    
    public void eliminar(Long id) {
        try {
            usuarioService.eliminar(id);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Usuario eliminado correctamente"));
            cargarLista();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }
    
    // Getters y Setters
    public List<Usuario> getListaUsuarios() {
        return listaUsuarios;
    }

    public void setListaUsuarios(List<Usuario> listaUsuarios) {
        this.listaUsuarios = listaUsuarios;
    }

    public Usuario getUsuarioSeleccionado() {
        return usuarioSeleccionado;
    }

    public void setUsuarioSeleccionado(Usuario usuarioSeleccionado) {
        this.usuarioSeleccionado = usuarioSeleccionado;
    }

    public Usuario getNuevoUsuario() {
        return nuevoUsuario;
    }

    public void setNuevoUsuario(Usuario nuevoUsuario) {
        this.nuevoUsuario = nuevoUsuario;
    }

    public String getModoFormulario() {
        return modoFormulario;
    }

    public void setModoFormulario(String modoFormulario) {
        this.modoFormulario = modoFormulario;
    }
}