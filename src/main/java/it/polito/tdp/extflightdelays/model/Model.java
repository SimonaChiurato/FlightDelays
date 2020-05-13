package it.polito.tdp.extflightdelays.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {
	
	private SimpleWeightedGraph<Airport, DefaultWeightedEdge> grafo;
	private Map<Integer, Airport> idMap;
	private ExtFlightDelaysDAO dao;
	
	private Map<Airport,Airport> visita;
	
	
	public Model() {
		idMap= new HashMap<>();
		dao= new ExtFlightDelaysDAO();
		this.dao.loadAllAirports(idMap);
		visita= new HashMap<>();
	}
	
	public void creaGrafo(int x) {
		this.grafo= new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		for(Airport a: idMap.values()) {
			if(dao.getAirlinesNumber(a)>x) {
				this.grafo.addVertex(a);
			}
		for(Rotta r: dao.getRotte(idMap)) {
			if(this.grafo.containsVertex(r.getArrivo()) && this.grafo.containsVertex(r.getPartenza())) {
			DefaultWeightedEdge e= this.grafo.getEdge(r.getArrivo(), r.getPartenza());
			if(e==null) {
				Graphs.addEdgeWithVertices(this.grafo, r.getArrivo(), r.getPartenza(), r.getPeso());
			}else {
				double pesoVecchio= this.grafo.getEdgeWeight(e);
				double pesoNuovo= pesoVecchio+r.getPeso();
				this.grafo.setEdgeWeight(e, pesoNuovo);
			}
		}
		}
		}
	}

	public int numeroVertici() {
		return this.grafo.vertexSet().size();
	}
	public int numeroArchi() {
		return this.grafo.edgeSet().size();
	}
	public Collection<Airport> getAirport(){
		
		return this.grafo.vertexSet();
	}
	
	public List<Airport> trovaPercorso(Airport a1, Airport a2){
		List<Airport> percorso= new ArrayList<>();
		visita.put(a1, null);
		
		BreadthFirstIterator<Airport, DefaultWeightedEdge> it= new BreadthFirstIterator<>(this.grafo,a1);
		
		it.addTraversalListener(new TraversalListener<Airport, DefaultWeightedEdge>() {
			
			@Override
			public void vertexTraversed(VertexTraversalEvent<Airport> e) {}
			
			@Override
			public void vertexFinished(VertexTraversalEvent<Airport> e) {}
			
			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> e) {
				Airport sorgente= grafo.getEdgeSource(e.getEdge());
				Airport destinazione= grafo.getEdgeTarget(e.getEdge());
				
				if(!visita.containsKey(destinazione) && visita.containsKey(sorgente)) {
					visita.put(destinazione, sorgente);
				}else if (!visita.containsKey(sorgente) && visita.containsKey(destinazione)){
					visita.put(sorgente, destinazione);
				}
			}
			
			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {}
			
			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {}
		});
		
		while(it.hasNext()) {
			it.next();
		}
		if(!visita.containsKey(a2)) {
			return null;
		}
		Airport step=a2;
		while(!step.equals(a1)) {
			percorso.add(step);
			step=visita.get(step);
		}
		percorso.add(a1);
		return percorso;
	}
}
