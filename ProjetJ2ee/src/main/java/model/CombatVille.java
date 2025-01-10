package model;

import java.util.UUID;

/**
 * Stocke l'état d'un combat en cours entre un soldat et une ville.
 */
public class CombatVille {
    private String combatId; // Identifiant unique du combat
    private Soldat soldatAttaquant;
    private Ville villeCible;

    // On peut réutiliser la logique de PV dans deux champs internes.
    // Le soldat a déjà ses pointsDeVie, la ville a pointsDeDefense.
    // Pour la cohérence "en cours de combat", on snapshot ou on peut modifier directement.
    private int pvSoldat;
    private int defVille;

    private boolean enCours;       // Vrai si le combat n'est pas terminé
    private boolean tourSoldat;    // Vrai si c'est au tour du soldat

    private int derniereValeurDe;

    public CombatVille(Soldat attaquant, Ville ville) {
        this.combatId = UUID.randomUUID().toString();
        this.soldatAttaquant = attaquant;
        this.villeCible = ville;
        this.pvSoldat = attaquant.getPointsDeVie();
        this.defVille = ville.getPointsDeDefense();
        this.enCours = true;
        this.tourSoldat = true; // Par défaut, le soldat commence
    }

    // Getters / Setters usuels
    public String getCombatId() {
        return combatId;
    }

    public Soldat getSoldatAttaquant() {
        return soldatAttaquant;
    }

    public Ville getVilleCible() {
        return villeCible;
    }

    public int getPvSoldat() {
        return pvSoldat;
    }

    public int getDefVille() {
        return defVille;
    }

    public boolean isEnCours() {
        return enCours;
    }

    public boolean isTourSoldat() {
        return tourSoldat;
    }

    public int getDerniereValeurDe() {
        return derniereValeurDe;
    }

    /**
     * Le soldat lance le dé et inflige des dégâts à la ville.
     */
    public void attaquerVille() {
        derniereValeurDe = (int) (Math.random() * 6) + 1;
        defVille -= derniereValeurDe;

        if (defVille <= 0) {
            defVille = 0;
            enCours = false; // La ville est conquise
        } else {
            // Toujours en cours => on passe la main à la ville
            tourSoldat = false;
        }

        // SI le combat se termine ICI et que le soldat est vivant => on recopie ses PV
        if (!enCours && pvSoldat > 0) {
            // => Le soldat est resté en vie, on met à jour ses PV réels
            soldatAttaquant.setPointsDeVie(pvSoldat);
            soldatAttaquant.getOwner().incrementerScore(500);
        }
    }

    public void riposteVille() {
        derniereValeurDe = (int) (Math.random() * 6) + 1;
        pvSoldat -= derniereValeurDe;

        if (pvSoldat <= 0) {
            pvSoldat = 0;
            enCours = false; // Le soldat meurt
        } else {
            // Toujours en cours => on repasse la main au soldat
            tourSoldat = true;
        }

        // SI le combat se termine ICI et que le soldat est vivant => on recopie ses PV
        if (!enCours && pvSoldat > 0) {
            // => soldat survit, donc on met à jour son Soldat (dans la partie)
            soldatAttaquant.setPointsDeVie(pvSoldat);
        }
    }
}