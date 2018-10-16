import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays; // sort()

class Sommet {
	int degre; // son degré. Veut dire degré sortant si orienté.
	int coul; // 0=existe pas 1=non visite par ParcoursLargeur 2=dans la file du PL 3=visite par PL
	int []adj; // tableau d'adjacence. une case = un numero de voisin. sa longueur est degré
	int dist; // sa distance à D, utilisé dans le parcours en largeur
} // pas de constructeur on affectera tous les champs plus tard

class Graphe { 
	boolean undirected; // vrai si non-oriente, faux si oriente
	int n;      // nombre de sommets "reels"
	int nmax;   // numero max d'un sommet
	int m;      // nombre d'arcs ou aretes
	int dmax;   // degre max d'un sommet
	Sommet[] V; // tableau des sommets. De taille nmax+1 normalement

	public void distri(){
		int [] t = new int [dmax];
		for(int i =0; i < nmax ;i++){
			if (V[i] !=null){
				t[V[i].degre] ++;
			}
		}
		for(int i : t){
			System.out.println(i + " " + t[i]);
		}
	}  // pas de constructeur on affectera tous les champs plus tard
}

public class TP1 {	
	public static  void lecture(Graphe G, String filename) {
		// lecture du graphe


		// passe 1 : compte les lignes

		int compteur = 0;
		try {
			BufferedReader read = new BufferedReader(new FileReader(filename));
			while (read.readLine() != null)
				compteur++;
			read.close(); 
			System.out.println(filename+ " fait "+compteur+" lignes.");
			System.out.println("Inits Mémoire allouée : " +  (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) + " octets ");
		}  catch (IOException e) {
			System.out.println("Erreur entree/sortie sur "+filename);
			System.exit(1);
		}

		// Passe 2 : lit le fichier et construit un tableau
		int l = 0;   // nombre de lignes d'aretes déjà lues
		int[][] lus = new int[compteur][2];
		try {
			BufferedReader read = new BufferedReader(new FileReader(filename));
			System.out.println("Pass 1 Mémoire allouée : " +  (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) + " octets ");

			while(true) {
				String line = read.readLine();
				if(line==null)
					break;
				if(line.charAt(0) == '#') {
					System.out.println(line);
					continue;
				}
				int a = 0;
				for (int pos = 0; pos < line.length(); pos++)
				{
					char c = line.charAt(pos);
					if(c==' ' || c == '\t')
					{
						if(a!=0)
							lus[l][0]=a;
						a=0;
						continue;
					}
					if(c < '0' || c > '9')
					{
						System.out.println("Erreur format ligne "+l+"c = "+c+" valeur "+(int)c);
						System.exit(1);
					}
					a = 10*a + c - '0';
				}
				lus[l][1]=a;
				if(G.nmax<lus[l][0]) // au passage calcul du numéro de sommet max
					G.nmax = lus[l][0];
				if(G.nmax<lus[l][1]) // au passage calcul du numéro de sommet max
					G.nmax = lus[l][1];
				l++;
			}
			read.close();
		}  catch (IOException e) {
			System.out.println("Erreur entree/sortie sur "+filename);
			System.exit(1);
		}
		System.out.println("pass 2 Mémoire allouée : " +  (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) + " octets ");

		// deuxième passe : alloue les sommets et calcule leur degre (sans tenir compte des doublons)
		int nbloop = 0;
		G.V = new Sommet[G.nmax+1];
		for(int i=0;i<=G.nmax;i++)
			G.V[i]=new Sommet();

		for(int i = 0; i< l; i++)
		{
			int x, y; // juste pour la lisibilité
			x = lus[i][0];
			y = lus[i][1];
			if(x==y) { // nous ignorons les boucles
				nbloop++;
				continue;
			}

			(G.V[x].degre)++; // si arc x->y augmente le degre de x 
			if(G.undirected)
				(G.V[y].degre)++; // ...et celui de y si arete x--y

			if(G.V[x].coul==0) { // le sommet x existe, sa couleur passe de 0 a 1
				G.V[x].coul=1;
				G.n++; // on incremente alors n
			}
			if(G.V[y].coul==0) { // idem pour l'autre cote de l'arc/arete
				G.V[y].coul=1;
				G.n++;
			}
		}
		if(nbloop > 0)
			System.out.println(nbloop + " boucles ont été ignorées");
		System.out.println("pass 3 Mémoire allouée : " +  (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) + " octets ");
		// troisieme passe : ajoute les arcs. 
		// d'abord allouons les tableaux d'adjacance
		for(int i=0;i<=G.nmax;i++) {
			if(G.V[i].degre>0)  
				G.V[i].adj = new int[G.V[i].degre];
			G.V[i].degre=0; // on remet le degre a zero car degre pointe la première place libre où insérer un élément pour la troisième passe
		}

		for(int i = 0; i< l; i++)
		{
			int x, y; // juste pour la lisibilité
			x = lus[i][0];
			y = lus[i][1];
			if(x==y)
				continue;
			G.V[x].adj[G.V[x].degre++] = y;
			// si non oriente on fait la meme chose dans l'autre sens
			if(G.undirected) 
				G.V[y].adj[G.V[y].degre++] = x;
		}

		System.out.println("pass 4 Mémoire allouée : " +  (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) + " octets ");
		// quatrieme passe : deboublonage, calul de m et des degres reels
		int nbdoubl=0;
		for(int i=0;i<=G.nmax;i++) {
			if(G.V[i].degre>0) { 
				Arrays.sort(G.V[i].adj); 		    // on commence par trier la liste d'adjacance.
				for(int j= G.V[i].degre-2;j>=0;j--)  
					if(G.V[i].adj[j]==G.V[i].adj[j+1]) {    // du coup les doublons deviennent consécutifs 
						// oh oh un doublon
						nbdoubl++;
						// on echange le doublon avec le dernier element que l'on supprime
						// boucle de droite a gauche pour eviter de deplacer un autre doublon
						G.V[i].adj[j+1]=G.V[i].adj[G.V[i].degre-1];
						G.V[i].degre--;
					}
			}
			// on calcule le degré max maintenant, et le nombre d'arêtes
			if(G.dmax < G.V[i].degre)
				G.dmax =  G.V[i].degre; 
			G.m+=G.V[i].degre;
		}
		if(G.undirected) // on a compté chaque arête deux fois et chaqyue doublon aussi
		{
			G.m/=2;
			nbdoubl /= 2;
		}
		if(nbdoubl >0)
			System.out.println(nbdoubl+" doublons ont ete supprimes");
	}

	public static int PL(Graphe G, int D, int A) {
		// parcours en largeur du graphe G depuis D 
		// Retourne le nombre d'accessibles depuis D
		ArrayDeque<Integer> file = new ArrayDeque<Integer>(); // file du parcours. On aurait pu faire <Sommet> aussi.
		file.add(D); // file = (D)
		G.V[D].dist=0; // D a distance 0 de lui-meme
		int rep=1; // Nombre d'accessibles. 1 car on a visité D
		while(!file.isEmpty()) { 
			int x = file.poll(); // extraire tete
			G.V[x].coul=3; // noir 
			int i;
			for(i=0;i<G.V[x].degre;i++) { // parcours des voisins
				int y = G.V[x].adj[i];
				if(G.V[y].coul==1) { // si blanc (et existe. coul==0 si inexistant)
					rep++; // on en a visite un de plus
					G.V[y].coul=2; // gris
					file.add(y);
					G.V[y].dist = 1+G.V[x].dist;
				}
			}
		}
		return rep;
	}

	public static void main(String[] args) 
	{
		if (args.length != 3) {
			System.out.println("Usage : java TP1 nomFichier.txt sommet1 sommet2");
			return;
		}

		Graphe G = new Graphe(); // le graphe sur lequel on travaille 
		G.undirected = true;     // on le suppose non-orienté
		int D = Integer.parseInt(args[1]); // sommet de depart du parcours
		int A = Integer.parseInt(args[2]); // sommet d'arrivée


		// 1- lecture 
		lecture(G, args[0]);
		System.out.println("nb aretes "+G.m+" -- nb sommets "+G.n+" -- num sommet max "+G.nmax+" -- degre max "+G.dmax);       
		System.out.println("Mémoire allouée : " +  (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) + " octets ");

		// 2- parcours
		if(D<0 || D>G.nmax || G.V[D].coul==0) 
			System.out.println("le sommet "+D+" n'existe pas dans le graphe");
		else {
			int rep  = PL(G,D,A);
			System.out.print(rep+" accessibles depuis le sommet "+D);
			if(G.V[A].dist>0)
				System.out.println(" et sa distance à "+A+" est "+G.V[A].dist);
			else
				System.out.println(" et "+A+" est inaccessible ou inexistant");
		}
		System.out.println("Mémoire allouée : " + (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) + " octets");
		G.distri();
	}
}

