    /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.gda.pg.eti.kask.projects_manager.form;

import java.io.Serializable;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import pl.gda.pg.eti.kask.projects_manager.entity.ProjHasUsers;
import pl.gda.pg.eti.kask.projects_manager.entity.ProjectDescription;
import pl.gda.pg.eti.kask.projects_manager.entity.Projects;
import pl.gda.pg.eti.kask.projects_manager.entity.UserRoles;
import pl.gda.pg.eti.kask.projects_manager.entity.Users;
import pl.gda.pg.eti.kask.projects_manager.facade.ProjHasUsersFacade;
import pl.gda.pg.eti.kask.projects_manager.facade.ProjectDescriptionFacade;
import pl.gda.pg.eti.kask.projects_manager.facade.ProjectsFacade;
import pl.gda.pg.eti.kask.projects_manager.facade.UsersFacade;
import pl.gda.pg.eti.kask.projects_manager.managers.ProjectsManager;

/**
 *
 * @author mateusz
 */
@Named
@ConversationScoped
public class EditHelperBean implements Serializable {

    @EJB
    private ProjectsFacade projectFacadeLocal;
    private Projects localProjects;
    private Projects projectBeforeChange;
    private UserRoles localRoles;
    private ProjectDescription editingProjDescription;
    private boolean editingProjects;
    @EJB
    private ProjHasUsersFacade projectUsersFacadeLocal;
    @EJB
    private ProjectDescriptionFacade projectDescriptionFacadeLocal;
    @EJB
    private UsersFacade usersFacadeLocal;
    private Users localUsers;
    private Users userBeforeChange;
    private boolean editingUsers;
    @Inject
    Conversation conversation;

    public Projects getProjects() {
        return localProjects;
    }

    public Users getLocalUsers() {
        return localUsers;
    }

    public void setLocalUsers(Users localUsers) {
        this.localUsers = localUsers;
    }

    private void beginConversation() {
        if (conversation.isTransient()) {
            conversation.begin();
        }
    }

    private void endConversation() {
        if (!conversation.isTransient()) {
            conversation.end();
        }
    }

    public Users getUser() {
        return localUsers;
    }

    public String newProject() {
        beginConversation();

        localProjects = new Projects();
        editingProjects = false;


        return "edit_projects";
    }

    public String editProject(Projects p) {
        beginConversation();

        localProjects = p;
        try {
            projectBeforeChange = (Projects) p.clone();
        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
        }
        editingProjects = true;

        return "edit_projects";

    }

    public ProjectDescription getEditingProjDescription() {
        return editingProjDescription;
    }

    public void setEditingProjDescription(ProjectDescription editingProjDescription) {
        this.editingProjDescription = editingProjDescription;
    }

    public String editProjDescription(ProjectDescription proj) {
        editingProjDescription = proj;

        return "edit_project_description";

    }

    public String saveProjDescription() {
        
        projectDescriptionFacadeLocal.edit(editingProjDescription);

        return "my_projects";
    }

    public String viewProject(Projects p) {
        localProjects = p;

        return "project_view";
    }

    public String addUsersToProject(Projects p) {
        localProjects = p;

        return "users_list";
    }



    public String saveReadOnlyUsers(Users u) {
        localUsers = u;
        
        projectFacadeLocal.edit(localProjects);
        return "edit_projects";
    }



    public boolean isOwnerInProject(Projects p, Users u) {
        for (ProjHasUsers pu : p.getProjHasUsersCollection()) {
            if (pu.getRola().getId().equals(1)) {
                if (pu.getUsers().getLogin().equals(u.getLogin())) {
                    return true;
                }
            }
        }
        return false;
    }

    public String display(Projects p, Users u) {
        if (u != null) {
            if (isOwnerInProject(p, u)) {
                return viewProject(p);
            } else if (p.getIsPariatlyPublic() || p.getIsPublic()) {
                return viewProject(p);
            } else {
                return "accessdenied";
            }
        } else if (p.getIsPublic()) {

            return viewProject(p);
        } else {
            return "accessdenied";
        }
    }

    public String saveUsersToProject(Users u, UserRoles roles) {
        if (ProjectsManager.addUser(localProjects, u, roles.getRoleName()) != true) {
            FacesContext.getCurrentInstance().addMessage("errorMessage", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Operacja dodania użytkownika nie powiodła się; prosimy o kontakt z aministratorem", null));
            return "edit_projects";
        } else {
            FacesContext.getCurrentInstance().addMessage("successMessage", new FacesMessage(FacesMessage.SEVERITY_INFO, "Operacja dodania użytkownika powiodła się!", null));
            localUsers = u;
            localProjects.addUserPerRole(u, roles.getId());
            projectFacadeLocal.edit(localProjects);
            return "edit_projects";
        }
    }

    public String removeUsersFromProject() {
        localUsers = (Users) FacesContext.getCurrentInstance()
                .getExternalContext().getRequestMap()
                .get("Users");

        
        projectFacadeLocal.edit(localProjects);
        return "edit_projects";
    }

    public String removeUsersFromProject(Users u) {
        if (ProjectsManager.removeUser(localProjects, u) != true) {
            FacesContext.getCurrentInstance().addMessage("errorMessage", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Operacja usunięcia użytkownika nie powiodła się; prosimy o kontakt z aministratorem", null));
            return "edit_projects";
        } else {
            FacesContext.getCurrentInstance().addMessage("successMessage", new FacesMessage(FacesMessage.SEVERITY_INFO, "Operacja usunięcia użytkownika powiodła się!", null));
            localUsers = u;
            localProjects.removeUserFromProject(u);
            projectFacadeLocal.edit(localProjects);
            return "edit_projects";
        }
        
    }

    public String newUser() {
        localUsers = new Users();
        editingUsers = false;

        return "edit_user";
    }

    public String saveUser() {
        if (editingUsers) {
            usersFacadeLocal.edit(localUsers);
        } else {
            usersFacadeLocal.create(localUsers);
        }
        return "projects_list";
    }

    private void saveNewProject(Users owner) {
        ProjectDescription desc = new ProjectDescription();
        desc.setProjFullName(localProjects.getProjName());
        localProjects.setProjDescription(desc);

        projectFacadeLocal.create(localProjects);
        localProjects.addUserPerRole(owner, 1);
        projectFacadeLocal.edit(localProjects);
    }

    private void saveEditingProject() {
        projectFacadeLocal.edit(localProjects);
    }

    public String saveProject(Users owner) {
        if (editingProjects) {
            if (ProjectsManager.editProject(projectBeforeChange, localProjects) != true) {
                FacesContext.getCurrentInstance().addMessage("errorMessage", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Operacja modyfikacji projektu nie powiodła się; prosimy o kontakt z aministratorem", null));
            } else {
                saveEditingProject();
                FacesContext.getCurrentInstance().addMessage("infoMessage", new FacesMessage(FacesMessage.SEVERITY_INFO, "Pomyślnie zmodyfikowano projekt " + localProjects.getProjName(), null));
            }
        } else {
            if (ProjectsManager.createProject(localProjects, owner) != true) {
                FacesContext.getCurrentInstance().addMessage("errorMessage", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Operacja utworzenia projektu nie powiodła się; prosimy o kontakt z aministratorem", null));
            } else {
                saveNewProject(owner);
                FacesContext.getCurrentInstance().addMessage("infoMessage", new FacesMessage(FacesMessage.SEVERITY_INFO, "Pomyślnie utworzono projekt " + localProjects.getProjName(), null));
            }
        }
        endConversation();
        return "my_projects";
    }

    public String usunProject() {

        if (ProjectsManager.deleteProject(localProjects) != true) {
            FacesContext.getCurrentInstance().addMessage("errorMessage", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Operacja usunięcia projektu nie powiodła się; prosimy o kontakt z aministratorem", null));
        } else {
            projectFacadeLocal.remove(localProjects);
            FacesContext.getCurrentInstance().addMessage("infoMessage", new FacesMessage(FacesMessage.SEVERITY_INFO, "Pomyślnie usunięto projekt " + localProjects.getProjName(), null));
        }

        endConversation();
        return "my_projects";
    }

    public String anuluj() {

        endConversation();
        return "my_projects";
    }

    public boolean isEditingProjects() {
        return editingProjects;
    }

    public void setEditingProjects(boolean editingProjects) {
        this.editingProjects = editingProjects;
    }

    public UserRoles getLocalRoles() {
        return localRoles;
    }

    public void setLocalRoles(UserRoles localRoles) {
        this.localRoles = localRoles;
    }

    @Remove
    public void destroy() {
        endConversation();
    }
}