package com.ensah.web;

import com.ensah.bo.Contact;
import com.ensah.bo.Groupe;
import com.ensah.service.IContactService;
import com.ensah.service.IGroupeService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
public class ContactController {

    protected final Logger LOGGER = Logger.getLogger(getClass());

    @Autowired
    IContactService contactService;

    @Autowired
    IGroupeService groupeService;
    @GetMapping({"/afficherForm","/"})
    public String afficherForm(Model model) {

        model.addAttribute("contactModel", new Contact());

        return "form";
    }

    @PostMapping("/ajoutContact")
    public String ajoutContact(@Valid @ModelAttribute("contactModel") Contact contact, BindingResult bindingResult,
                            ModelMap model) {


        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMsg", "Les données sont invalides.");
            LOGGER.warn("Erreur de validation du formulaire");
        } else {
            contactService.creeContact(contact);
            model.addAttribute("infoMsg", "Contact ajouté avec succès");
        }

        return "form";

    }

    @GetMapping("/afficherContacts")
    public String afficherContacts(ModelMap model) {

        model.addAttribute("listContacts",contactService.afficherContactsParOrdre());

        return "Contacts";

    }

    @GetMapping("/supprimerContact/{id}")
    public String supprimerContact(@PathVariable Long id) {

        Contact cnt = contactService.getContactById(id);
        Groupe grp = cnt.getGrpC();
        if(grp!= null){
            grp.getContact().remove(cnt);
            groupeService.modifierGroupe(grp);
        }

        contactService.supprimerContact(id);

        return "redirect:/afficherContacts";
    }

    @RequestMapping("/modifierForm/{id}")
    public String modifierForm(Model model,@PathVariable Long id) {
        Contact cnt = contactService.getContactById(id);
        model.addAttribute("contactModel", cnt);
        return "editForm";
    }

    @PostMapping("/modifierContact")
    public String modifierContact(@Valid @ModelAttribute("contactModel") Contact contact, BindingResult bindingResult, ModelMap model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMsg", "Les données sont invalides.");
            LOGGER.warn("Erreur de validation du formulaire");
        } else {
            contactService.modifierContact(contact);
            model.addAttribute("infoMsg", "Contact modifié avec succès");
        }
        return "editForm";
    }

    @RequestMapping("/affectationForm/{id}")
    public String affectationForm(Model model, @PathVariable Long id) {

        Contact cnt = contactService.getContactById(id);
        model.addAttribute("contactModel", cnt);
        model.addAttribute("listGroupes", groupeService.afficherGroupe());
        return "affectation";

    }

    @PostMapping("/affecterContactGrp")
    public String affecterContactGrp(@RequestParam("grpC") Long idGroupe,
                                     @RequestParam("idContact") Long idContact) {

        Contact cnt = contactService.getContactById(idContact);
        if(idGroupe == null){
            cnt.setGrpC(null);
        }
        else{
            Groupe grp = groupeService.getGroupeById(idGroupe);
            cnt.setGrpC(grp);
            contactService.modifierContact(cnt);
        }

        return "redirect:/afficherContacts";
    }

    @RequestMapping("/supprimerAffectation/{idContact}")
    public String supprimerAffectation(@PathVariable("idContact") Long idContact) {

        Contact cnt = contactService.getContactById(idContact);
        Groupe grp = cnt.getGrpC();
        if(grp!= null){
            grp.getContact().remove(cnt);
            groupeService.modifierGroupe(grp);
            cnt.setGrpC(null);
        }
        contactService.modifierContact(cnt);

        return "redirect:/afficherContacts";
    }

    @GetMapping("/rechercherNomContact")
    public String rechercherContactNom(Model model) {

        model.addAttribute("contactModel", new Contact());

        return "RechercherNom";
    }

    @PostMapping("/NomContactrechercher")
    public String contactNomRechercher(@Valid @ModelAttribute("contactModel") Contact contact, BindingResult bindingResult,
                               ModelMap model) {

        if (bindingResult.hasFieldErrors("nom")){
            model.addAttribute("errorMsg", "Les données sont invalides.");
            LOGGER.warn("Erreur de validation du formulaire");
        } else {
            List<Contact> contactR = contactService.RechercheParNom(contact.getNom());
            if (contactR.isEmpty()) {
                model.addAttribute("errorMsg", "Aucun contact trouvé");
            } else {
                model.addAttribute("contactR",contactR);
                model.addAttribute("infoMsg", "Contact cherché");
            }
        }
        return "RechercherNom";

    }

    @GetMapping("/rechercherNumContact")
    public String rechercherNumContact(Model model) {

        model.addAttribute("contactModel", new Contact());

        return "RechercherNum";
    }

    @PostMapping("/ContactrechercherNum")
    public String contactNumRechercher(ModelMap model, @RequestParam("telephone") String telephone) {

        if(telephone!=null && telephone.matches("^^06\\d{8}$")){
            Contact Rcontact=contactService.RechercheParNum(telephone);
            if(Rcontact!=null){
                model.addAttribute("contactR",Rcontact);
                model.addAttribute("infoMsg", "Contact cherché");
            }else {
                model.addAttribute("errorMsg", "Aucun contact trouvé");
            }
        }else{
            model.addAttribute("errorMsg", "Les données sont invalides.");
            LOGGER.warn("Erreur de validation du formulaire");
        }

        return "RechercherNum";

    }

    @GetMapping({"/groupeForm"})
    public String groupeForm(Model model) {

        model.addAttribute("grpModel", new Groupe());

        return "groupeForm";
    }

    @PostMapping("/ajoutGroupe")
    public String ajoutGroupe(@Valid @ModelAttribute("grpModel") Groupe grp, BindingResult bindingResult,
                               ModelMap model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMsg", "Les données sont invalides.");
            LOGGER.warn("Erreur de validation du formulaire");
        } else {
            try {
                groupeService.creeGroupe(grp);
                model.addAttribute("infoMsg", "Groupe ajouté avec succès");
            }catch (DataIntegrityViolationException ex){
                model.addAttribute("errorMsg", "Groupe deja existe");
                LOGGER.error("Erreur de unique groupe" + ex.getMessage());
            }
        }

        return "groupeForm";

    }

    @GetMapping("/afficherGroupes")
    public String afficherGroupes(ModelMap model) {

        model.addAttribute("listGroupes",groupeService.afficherGroupe());

        return "Groups";
    }

    @RequestMapping("/GroupeContact/{id}")
    public String afficherGroupeContact(Model model,@PathVariable Long id) {
        Groupe grp = groupeService.getGroupeById(id);
        model.addAttribute("grpModel", grp);
        model.addAttribute("listContacts",grp.getContact());
        return "contactOfGroupe";
    }

    @GetMapping("/rechercherGroupe")
    public String rechercherNomGroupe(Model model) {

        model.addAttribute("grpModel", new Groupe());

        return "RechercherGrpNom";
    }

    @PostMapping("/groupeRechercher")
    public String groupeNomRechercher(@Valid @ModelAttribute("grpModel") Groupe grp, BindingResult bindingResult,
                                       ModelMap model) {

        if (bindingResult.hasFieldErrors("nom")){
            model.addAttribute("errorMsg", "Les données sont invalides.");
            LOGGER.warn("Erreur de validation du formulaire");
        } else {
            List<Groupe> groupeR=groupeService.RechercheParNom(grp.getNom());
            if (groupeR.isEmpty()) {
                model.addAttribute("errorMsg", "Aucun groupe trouvé");
            } else {
                model.addAttribute("groupeR",groupeR);
                model.addAttribute("infoMsg", "Groupe cherché");
            }
        }
        return "RechercherGrpNom";

    }

    @RequestMapping("/contactAjoutGroupe/{id}")
    public String contactAjoutGroupe(Model model,@PathVariable Long id) {

        Groupe grp=groupeService.getGroupeById(id);
        model.addAttribute("grpModel", grp);
        model.addAttribute("listContacts",contactService.afficherContactsParOrdre());
        return "contactAjoutGroupe";
    }

    @GetMapping("/contactDansGroupe/{id}/{idContact}")
    public String contactDansGroupe(@PathVariable Long idContact, @PathVariable Long id) {

        Groupe grp=groupeService.getGroupeById(id);
        grp.getContact().add(contactService.getContactById(idContact));
        groupeService.modifierGroupe(grp);


        return "redirect:/contactAjoutGroupe/"+id;
    }

    @GetMapping("/supprimerGroupe/{id}")
    public String supprimerGroupe(@PathVariable Long id) {

        Groupe grp=groupeService.getGroupeById(id);
        for (Contact cnt : grp.getContact()) {
            cnt.setGrpC(null);
            contactService.modifierContact(cnt);
        }
        groupeService.supprimerGroupe(id);
        return "redirect:/afficherGroupes";
    }

    @RequestMapping("/modifierFormGrp/{id}")
    public String modifierFormGrp(Model model,@PathVariable Long id) {
        Groupe grp = groupeService.getGroupeById(id);
        model.addAttribute("grpModel", grp);

        return "editFormGrp";
    }

    @PostMapping("/modifierGroupe")
    public String modifierGroupe(@Valid @ModelAttribute("grpModel") Groupe grp, BindingResult bindingResult,
                                  ModelMap model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMsg", "Les données sont invalides.");
            LOGGER.warn("Erreur de validation du formulaire");
        } else {
            groupeService.modifierGroupe(grp);
            model.addAttribute("infoMsg", "Groupe modifié avec succès");
        }
        return "editFormGrp";
    }

    @RequestMapping("/supprimerContactGroupe/{id}/{idContact}")
    public String supprimerContactGroupe(Model model,@PathVariable Long id, @PathVariable Long idContact) {

        Groupe grp = groupeService.getGroupeById(id);
        Contact cnt = contactService.getContactById(idContact);
        grp.getContact().remove(contactService.getContactById(idContact));
        cnt.setGrpC(null);
        contactService.modifierContact(cnt);
        groupeService.modifierGroupe(grp);

        return "redirect:/GroupeContact/"+id;
    }
}