import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.serialization.GtfsReader;
//import org.onebusaway.gtfs.model.Trip;
//import org.onebusaway.gtfs.model.AgencyAndId;
//import org.onebusaway.gtfs.model.Stop;


import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.io.File;
import java.io.IOException;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.time.format.DateTimeFormatter;
//import java.util.stream.Collectors;




public class BussApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Input the GTFS files folder path:");
        String gtfsFolderPath = scanner.nextLine();

        System.out.println("Input data: <station_id> <number_of_buses> <timeFormat>");
        String input = scanner.nextLine();
        scanner.close();

        String [] parts = input.split(" ");

        String stationId = parts[0];
        String numFollowingBuses = parts[1];
        String timeFormat = parts[2];


        try {
            GtfsDaoImpl gtfsDao = loadGtfsData(gtfsFolderPath);


            List<StopTime> relevantStationStopTimes = gtfsDao.getAllStopTimes().stream()
                    .filter(stopTime -> stopTime.getStop().getId().getId().equals(stationId))
                    .sorted(Comparator.comparing(StopTime::getArrivalTime))
                    .filter(stopTime -> {
                        LocalTime currentTime = LocalTime.now();
                        LocalTime timeTwoHoursLater = currentTime.plusHours(2);
                        LocalTime arrivalTime = LocalTime.ofSecondOfDay(stopTime.getArrivalTime());

                        return arrivalTime.isAfter(currentTime) && arrivalTime.isBefore(timeTwoHoursLater);
                    })
                    .toList();

            List<String> buses = relevantStationStopTimes.stream()
                    .map(stopTime -> stopTime.getTrip().getRoute().getId().getId())
                    .distinct()
                    .limit(Long.parseLong(numFollowingBuses))
                    .toList();

            for (String bus : buses){
                System.out.print(bus+": ");
                for(StopTime stopTime : relevantStationStopTimes){
                    if(stopTime.getTrip().getRoute().getId().getId().equals(bus)){
                        printTime(LocalTime.ofSecondOfDay(stopTime.getArrivalTime()), timeFormat);
                    }
                }
                System.out.println();
            }



        } catch (IOException e) {
            System.err.println("Error loading GTFS data: " + e.getMessage());
            System.exit(1);
        }
    }

    private static GtfsDaoImpl loadGtfsData(String gtfsFolderPath) throws IOException {
        GtfsDaoImpl gtfsDao = new GtfsDaoImpl();

        GtfsReader reader = new GtfsReader();
        reader.setInputLocation(new File(gtfsFolderPath));

        reader.setEntityStore(gtfsDao);
        reader.run();

        return gtfsDao;
    }

    public static void printTime(LocalTime time, String format){
        if(format.equals("relative")){
            System.out.print(Duration.between(LocalTime.now(),time).toMinutes()+" minutes, ");
        }
        else {
            System.out.print(time+", ");
        }
    }

}
