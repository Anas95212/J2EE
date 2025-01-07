package model;
 
import java.util.UUID;
 
/**
* Stocke l'état d'un combat en cours entre deux soldats.
*/
public class Combat {
 
    private String combatId;   // Identifiant unique du combat
    private Soldat soldat1;
    private Soldat soldat2;
    private int pvSoldat1;
    private int pvSoldat2;
    private boolean enCours;        // Vrai si le combat n'est pas terminé
    private boolean tourSoldat1;    // Vrai si c'est au tour de soldat1
    private int derniereValeurDe;
 
    public Combat(Soldat s1, Soldat s2) {
        this.combatId = UUID.randomUUID().toString();
        this.soldat1 = s1;
        this.soldat2 = s2;
        this.pvSoldat1 = s1.getPointsDeVie();
        this.pvSoldat2 = s2.getPointsDeVie();
        this.enCours = true;
        this.tourSoldat1 = true; // Par défaut, soldat1 commence
    }
 
    public String getCombatId() {
        return combatId;
    }
 
    public Soldat getSoldat1() {
        return soldat1;
    }
 
    public Soldat getSoldat2() {
        return soldat2;
    }
 
    public int getPvSoldat1() {
        return pvSoldat1;
    }
 
    public int getPvSoldat2() {
        return pvSoldat2;
    }
 
    public boolean isEnCours() {
        return enCours;
    }
 
    public boolean isTourSoldat1() {
        return tourSoldat1;
    }
 
    public int getDerniereValeurDe() {
        return derniereValeurDe;
    }
 
    /**
     * Lancer le dé (1-6) et infliger les dégâts à l'adversaire.
     */
    public void lancerDeEtAttaquer() {
        derniereValeurDe = (int) (Math.random() * 6) + 1;
 
        if (tourSoldat1) {
            pvSoldat2 -= derniereValeurDe;
            if (pvSoldat2 <= 0) {
                pvSoldat2 = 0;
                enCours = false;
            }
        } else {
            pvSoldat1 -= derniereValeurDe;
            if (pvSoldat1 <= 0) {
                pvSoldat1 = 0;
                enCours = false;
            }
        }
 
        if (enCours) {
            tourSoldat1 = !tourSoldat1;
        }
    }
}
 