package com.chyb;

import com.chyb.Entity.Animal;
import com.chyb.Entity.Plant;
import com.chyb.utils.Vector2D;

import java.util.*;

public class WorldMap {

    private int width, height;
    private int jungleWidth, jungleHeight;
    private HashMap<Vector2D, LinkedList<Animal> > animalMap;
    private HashMap<Vector2D, Plant> plantMap;
    private ArrayList<Animal> animalList;
    private static Random random = new Random();

    public WorldMap(int width, int height, int jungleWidth, int jungleHeight){
        this.width = width;
        this.height = height;
        this.jungleWidth = jungleWidth;
        this.jungleHeight = jungleHeight;
        animalList = new ArrayList<Animal>();
        animalMap = new HashMap<Vector2D, LinkedList<Animal> >();
        plantMap = new HashMap<Vector2D, Plant>();

    }
    private void generateStartingAnimals(int amount){
        for(int i = 0; i < amount; i++){
            while(true){
                Vector2D newPosition = new Vector2D(random.nextInt()% width, random.nextInt() % height);
                if(!animalMap.containsKey(newPosition)){
                    Animal animal = new Animal(newPosition,this);
                    LinkedList<Animal> ll = new LinkedList<>();
                    ll.add(animal);
                    animalMap.put(newPosition, ll);
                    animalList.add(animal);
                    break;
                }
            }
        }
    }
    private void addPlants(int amountPerZone){
        for(int i = 0; i<amountPerZone; i++){
            boolean foundSpot = false;
            int randomX=0, randomY=0;
            int missedAmount = 0;
            while(!foundSpot && missedAmount < width*height) {
                randomX = random.nextInt() % (width - jungleWidth);
                randomY = random.nextInt() % (height - jungleHeight);
                if(randomX > (width-jungleWidth)/2) randomX += jungleWidth;
                if(randomY > (height-jungleHeight)/2) randomY += jungleHeight;
                Vector2D randomVector = new Vector2D(randomX, randomY);
                foundSpot = !isOccupiedByAnimal(randomVector) && !isOccupiedByPlant(randomVector);
                missedAmount++;
            }
            if(foundSpot) plantMap.put(new Vector2D(randomX, randomY), new Plant(new Vector2D(randomX,randomY)));
            //TODO refactor test
            //jungleAdd
            missedAmount = 0;
            while(!foundSpot && missedAmount < jungleWidth*jungleHeight) {
                randomX = random.nextInt() % (jungleWidth) + (height - jungleHeight)/2;
                randomY = random.nextInt() % (jungleHeight) + (width - jungleWidth)/2;
                Vector2D randomVector = new Vector2D(randomX, randomY);
                foundSpot = !isOccupiedByAnimal(randomVector) && !isOccupiedByPlant(randomVector);
                missedAmount++;
            }
            if(foundSpot) plantMap.put(new Vector2D(randomX, randomY), new Plant(new Vector2D(randomX,randomY)));

        }

    }

    private boolean isOccupiedByPlant(Vector2D position) {
        return plantMap.containsKey(position);
    }

    private void addChild(Vector2D position, Animal parent1, Animal parent2){
        ArrayList<Vector2D> openSpaces = new ArrayList<Vector2D>();

        for(int i=-1; i<=1; i++){
            for(int j=-1; j<=1; j++){
                Vector2D newPosition = position.add(new Vector2D(i, j));
                if(!isOccupiedByAnimal(position.add(new Vector2D(i, j)))){
                    openSpaces.add(newPosition);
                }
            }
        }
        //TODO ???
        if(openSpaces.isEmpty()) return;
        Vector2D childPosition = openSpaces.get(random.nextInt() % openSpaces.size());

        Animal animal = new Animal(childPosition, parent1, parent2, this);
        animalList.add(animal);
        animalMap.get(position).add(animal);
    }
    public void eatPlants(){
        ArrayList<Vector2D> toRemove = new ArrayList();
        for(Vector2D plantPosition : plantMap.keySet()){
            if(!animalMap.containsKey(plantPosition)) continue;
            toRemove.add(plantPosition);
            LinkedList<Animal> eatingAnimals = animalMap.get(plantPosition);
            ArrayList<Animal> bestAnimals = new ArrayList<Animal>();

            int bestEnergy = -1;
            for(Animal animal : eatingAnimals){
                if(bestEnergy < animal.getEnergy()){
                    bestAnimals.clear();
                    bestAnimals.add(animal);
                } else if(bestEnergy == animal.getEnergy()){
                    bestAnimals.add(animal);
                }
            }
            for(Animal animal : bestAnimals){
                animal.addEnergy(Main.plantEnergy);
            }
            //TODO sharing energy
        }
        for(Vector2D plantPosition : toRemove){
            plantMap.remove(plantPosition);
        }
    }
    public void handleBirths(){
        for(Vector2D animalPosition : animalMap.keySet()){
            LinkedList<Animal> animalLl = animalMap.get(animalPosition);
            if(animalLl.size()>1) {
                Collections.sort(animalLl);
                addChild(animalPosition, animalLl.get(0), animalLl.get(1));
            }
        }
    }
    public void cycle(){

        for(int i=0;i<animalList.size();i++){
            if(animalList.get(i).getEnergy() <= 0){
                animalList.remove(i);
                i--;
                continue;
            }
            animalList.get(i).move();
        }
        eatPlants();
        handleBirths();
        addPlants(1);
    }

    private boolean isOccupiedByAnimal(Vector2D position){
        if(! animalMap.containsKey(position)) return false;
        else return !animalMap.get(position).isEmpty();
    }

    public void moveAnimal(Animal animal, Vector2D oldPosition, Vector2D newPosition) {
        //remove from oldPosition
        LinkedList<Animal> animalLl = animalMap.get(oldPosition);
        animalLl.remove(animal);

        //add to newPosition
        if(animalMap.containsKey(newPosition)){
            animalMap.get(newPosition).add(animal);
        }else{
            animalMap.put(newPosition, new LinkedList<Animal>(Collections.singleton(animal)));
        }
    }

    public Vector2D getSize() {
        return new Vector2D(width,height);
    }
}