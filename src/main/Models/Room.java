import java.time.DayOfWeek;
import java.time.LocalDate;

public class Room {
    private int roomID;
    private int roomNum;
    private int isOccupied;
    private double price;
    private double weekendRate;
    private double weekendPrice;
    private String type;

    public Room(){}

    public Room(int roomID, int roomNum, double price, String type){
        this.roomID = roomID;
        this.roomNum = roomNum;
        this.price = price;
        this.type = type;
        this.weekendRate = 0.05;
        this.weekendPrice = this.price * (1.0+this.weekendRate);
    }

    public String getType(){
        return this.type;
    }

    public void setType(String type){
        this.type = type;
    }

    public void setPrice(double price){
        this.price = price;
    }

    public void setWeekendRate(double weekendRate){
        this.weekendRate = weekendRate;
    }

    public void setRoomID(int id){this.roomID = id;}

    public void setRoomNum(int num){this.roomNum = num;}

    public void setIsOccupied(int occupied){
        this.isOccupied = occupied;
    }

    public double getWeekendPrice(){
        return this.weekendPrice;
    }

    public double getPrice(){
        return this.price;
    }

    public int getRoomID(){
        return this.roomID;
    }

    public int getRoomNum(){
        return this.roomNum;
    }

    public int getIsOccupied(){return this.isOccupied;}

    public void recalculateWeekendPrice(){
        this.weekendPrice = this.price * (1.0+this.weekendRate);
    }

}
