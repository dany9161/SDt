import java.sql.Time;

public class Processor {
    String url;

    public String getUrl() {
        return url;
    }

    Time lastHeartBeat;

    public Processor(String url) {
        this.url = url;
        lastHeartBeat = new Time(System.currentTimeMillis());
    }

    public void updateLastHeartBeat(){
        lastHeartBeat = new Time(System.currentTimeMillis());
    }

    public boolean hasFailed(){
        return (System.currentTimeMillis()- lastHeartBeat.getTime()) > 30000; //milisegundos
    }
}
