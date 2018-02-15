import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Semaphore;

/**
 * Created by ASUS on 12/24/2017.
 */


public class mainclass {
    public static class Myelevator extends Thread{
        Semaphore []in;
        Semaphore[]out;
        Semaphore passenger;
        int countPassenger=0;
        int currentState=1;
        int up_down=0;
        int maxFloor=0;
        int minFloor=10;
        Myelevator(Semaphore[] in1,Semaphore[] out1){
            in=in1;
            out=out1;
            passenger=new Semaphore(0,true);
        }
        void callElavator(Person p)  {
            int pos=p.getPosition();
            System.out.println(p.name+" is calling elavator from "+pos);
            if(up_down==0){
                if(pos>currentState){
                    up_down=1;
                }else{
                    if(pos<currentState) {
                        up_down = -1;
                    }
                }
            }
            if(pos>currentState){
                maxFloor=java.lang.Math.max(maxFloor,pos);
            }else{
                if(pos<currentState) {
                    minFloor=java.lang.Math.min(minFloor,pos);
                }
            }
            countPassenger++;
            if(countPassenger==1){
                passenger.release();
            }
            try {
                //       System.out.println("pos: "+pos);
                out[pos].acquire();
            } catch (InterruptedException e) {
                System.out.println("error "+ pos + " :"+p.name);
                e.printStackTrace();

            }


        }
        void setElavator(Person p) throws InterruptedException {
            int dest=p.getDestination();
            System.out.println(p.name+" set elavator to"+dest);
            if(dest>currentState){
                maxFloor=java.lang.Math.max(maxFloor,dest);
            }else{
                if(dest<currentState) {
                    minFloor=java.lang.Math.min(minFloor,dest);
                }
            }
            in[dest].acquire();

        }
        public void run() {
            boolean valid=true;
            while(true) {

                System.out.println("floor elavator" + currentState);
                //      System.out.println("number passenger waiting in:" + out[currentState].getQueueLength());
                for (int i = 0; i < out[currentState].getQueueLength(); i++) {
                    out[currentState].release();

                }
                //     System.out.println("number passenger exit in:" + in[currentState].getQueueLength());
                for (int i = 0; i < in[currentState].getQueueLength(); i++) {
                    in[currentState].release();
                    countPassenger--;
                }
                //System.out.println("11");
                if (up_down == 1) {
                    currentState++;
                } else {
                    if(up_down==-1) {
                        currentState--;
                    }
                }
                if(currentState==maxFloor+1 && up_down==1){
                    currentState=maxFloor-1;
                    up_down=-1;
                    maxFloor=getMax();
                }
                if(currentState==minFloor-1 &&up_down==-1 ){
                    currentState=minFloor+1;
                    up_down=1;
                    minFloor=getMin();
                }
                 try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("count Passenger: "+countPassenger);
                if (countPassenger == 0) {
                    if (currentState == 1) {
                        try {
                            System.out.println("elavator in 1 floor waiting for passenger");
                            up_down=0;
                            System.out.println(passenger.getQueueLength());
                            valid=true;
                            passenger.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("elevator go to floor 1to reset");
                        up_down = -1;
                    }
                }
            }
        }
        int getMax(){
            int i=0;
            for(i=10;i>=0;i--){
                if(in[i].getQueueLength()!=0 && out[i].getQueueLength()!=0){
                    return i;
                }
            }
            return 0;
        }
        int getMin(){
            for(int i=0;i<11;i++){
                if(in[i].getQueueLength()!=0 && out[i].getQueueLength()!=0){
                    return i;
                }
            }
            return 10;
        }
    }
    public static class Person extends Thread{
        String name;
        int position;
        int destination;
        Myelevator elavat;
        int counter=0;
        Random rand = new Random();


        Person(String me,Myelevator me_elev){
            name=me;
            elavat=me_elev;
            position=rand.nextInt(10)+1;
            destination= rand.nextInt(9)+1;
        }
        public int getPosition(){
            return position;
        }
        public int getDestination(){
            return destination;
        }
        public void run() {
            while(counter!=2) {
                //System.out.println(name + "is waiting for elaavtor");
                elavat.callElavator(this);

                System.out.println(name + " enter elaavtor");
                try {
                    elavat.setElavator(this);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(name + " exit elavator");
                position=destination;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                counter++;
                destination = rand.nextInt(9) + 1;

            }


        }
    }
    public static void main(String[] args) throws InterruptedException {
        Semaphore[] out=new Semaphore[11];
        Semaphore[] in=new Semaphore[11];
        for (int i=0;i<11;i++){
            in[i]=new Semaphore(0,true);
            out[i]=new Semaphore(0,true);
        }
        Myelevator me=new Myelevator(in,out);
        me.start();
        Person[] persons=new Person[4];
        for(int i=0;i<4;i++){
            persons[i]=new Person("passenger"+i,me);
            persons[i].start();
        }

    }
}

