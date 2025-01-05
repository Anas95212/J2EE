package model;

import java.util.UUID;

/**
 * Stocke l'état d'un combat en cours entre deux soldats.
 */
public class Combat {

    private String combatId;         // Identifiant unique du combat
    private Soldat soldat1;
    private Soldat soldat2;
    private int pvSoldat1;
    private int pvSoldat2;

    private boolean enCours;         // Indique si le combat est toujours en cours
    private boolean tourSoldat1;     // true si c’est au tour du soldat1, false sinon

    public Combat(Soldat s1, Soldat s2) {
        this.combatId = UUID.randomUUID().toString(); 
        this.soldat1 = s1;
        this.soldat2 = s2;
        this.pvSoldat1 = s1.getPointsDeVie();
        this.pvSoldat2 = s2.getPointsDeVie();
        this.enCours = true;
        this.tourSoldat1 = true; // On peut décider que soldat1 commence
    }

    // Getters / setters
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

    /**
     * Méthode pour infliger des dégâts (1 à 6) au soldat adverse.
     */
    public void lancerDeEtAttaquer() {
        int degats = 1 + (int)(Math.random() * 6); // 1 à 6

        if (tourSoldat1) {
            // soldat1 attaque soldat2
            pvSoldat2 -= degats;
            if (pvSoldat2 <= 0) {
                pvSoldat2 = 0;
                enCours = false;
            }
        } else {
            // soldat2 attaque soldat1
            pvSoldat1 -= degats;
            if (pvSoldat1 <= 0) {
                pvSoldat1 = 0;
                enCours = false;
            }
        }

        // Changement de tour si le combat n’est pas fini
        if (enCours) {
            tourSoldat1 = !tourSoldat1;
        }
    }
}
