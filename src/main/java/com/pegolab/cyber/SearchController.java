package com.pegolab.cyber;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SearchController {

    private static final int MINIMUM_LOG_FETCH_TIME = 5;
    Instant lastFetch;
    static boolean firstRun = true;

    @GetMapping("/")
    public String showSearchForm(Model model) {
        model.addAttribute("searchForm", new SearchForm());
        return "searchForm";
    }

    @PostMapping("/search")
    public String search(SearchForm searchForm, Model model) {
        // Implement search logic here
        String searchString = searchForm.getSearchString();
        // Perform search in file and get results
        List<ActivityLog> searchResults = searchInFile(searchString);
        model.addAttribute("searchResults", searchResults);
        model.addAttribute("totalDuration", ActivityLog.getTotalDuration());
        model.addAttribute("code", searchString);
        return "searchResults";
    }

    // Method to search for a string in a file
    private List<ActivityLog> searchInFile(String searchString) {
        // Implement search logic here
        // Read file line by line and check for the search string
        // Return list of lines containing the search string
        String logDirectory = "/home/parfait/Programming/rb4011/FauconRB4011/log";
        fetchLogs();
        List<String> fileList = Stream.of(new File(logDirectory).listFiles())
                            .filter(file -> !file.isDirectory())
                            .map(File::getPath)
                            .sorted(new AlphanumComparator())
                            .collect(Collectors.toList());
        List<String> logsLines = new ArrayList<>();
        int size = fileList.size();
        for(int i = size - 1; i >= 0; i--) {
            try {
                logsLines.addAll(Files.readAllLines(Paths.get(fileList.get(i))));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        List<String> codeLogs = logsLines.stream().filter(line -> line.matches(".*"+searchString+".*logged.*")).collect(Collectors.toList());

        List<LogEvent> logEvents = new ArrayList<>();
        String toSearchIn = "logged in";
        for(String logLine : codeLogs) {
            String[] logPart = logLine.split(" ");
            LocalDateTime ldt = LocalDateTime.of(LocalDate.parse(logPart[0], DateTimeFormatter.ofPattern("MMM/dd/uuuu")), LocalTime.parse(logPart[1]));
            String ipAddress = logPart[5].substring(1, logPart[5].length()-2);
            logEvents.add(new LogEvent(ldt, ipAddress, logLine.contains(toSearchIn)));
        }
        Collections.sort(logEvents);

        LocalDateTime inLdt = null, outLdt = null;
        String inIpAddress = null, outIpAddress = null;
        boolean loggin = false;
        Duration totalDuration = Duration.ZERO;
        List<ActivityLog> activityLogs = new ArrayList<>();

        for(LogEvent logEvent : logEvents) {
            if(logEvent.isLogIn()) {
                if(loggin) {
                    ActivityLog activityLog = new ActivityLog(null, null, null, null);
                    activityLog.setStartDate(inLdt.toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                    .withLocale(Locale.FRANCE)
                    .withZone(ZoneId.systemDefault())));
                    activityLog.setStartTime(inLdt.toLocalTime());
                    activityLog.setIpAddress(inIpAddress);
                }
                inLdt = logEvent.getLocalDateTime();
                inIpAddress = logEvent.getIpAddress();
                loggin = true;
            } else {
                outLdt = logEvent.getLocalDateTime();
                outIpAddress = logEvent.getIpAddress();
                if(loggin && outLdt.isAfter(inLdt)) {
                    ActivityLog activityLog = new ActivityLog(null, null, null, null);
                    activityLog.setStartDate(inLdt.toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                    .withLocale(Locale.FRANCE)
                    .withZone(ZoneId.systemDefault())));

                    activityLog.setStartTime(inLdt.toLocalTime());
                    activityLog.setEndDate(outLdt.toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                    .withLocale(Locale.FRANCE)
                    .withZone(ZoneId.systemDefault())));
                    activityLog.setEndTime(outLdt.toLocalTime());
                    Duration d = Duration.between(inLdt, outLdt);
                    totalDuration = totalDuration.plus(d);
                    activityLog.setDuration(durationFormat(d));
                    if(inIpAddress.equals(outIpAddress)) {
                        activityLog.setIpAddress(inIpAddress);
                    } else {
                        activityLog.setIpAddress(inIpAddress+"->"+outIpAddress);
                    }
                    activityLogs.add(activityLog);
                    loggin = false;
                } else if(!loggin) {
                    ActivityLog activityLog = new ActivityLog(null, null, null, null);
                    activityLog.setEndDate(outLdt.toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                    .withLocale(Locale.FRANCE)
                    .withZone(ZoneId.systemDefault())));
                    activityLog.setEndTime(outLdt.toLocalTime());
                    activityLog.setIpAddress(outIpAddress);
                    activityLogs.add(activityLog);
                }
            }
        }
        ActivityLog.setTotalDuration(durationFormat(totalDuration));
        return activityLogs;
    }

    private void fetchLogs() {
        Instant currentFetch = Instant.now();
        if(firstRun) {
            lastFetch = Instant.now();
            firstRun = false;
        } else {
            if(Duration.between(lastFetch, currentFetch).toMinutes() < MINIMUM_LOG_FETCH_TIME) {
                return;
            }
        }

        String[] commands = {"/home/parfait/Programming/rb4011/wget-download"};
        try {
            Runtime.getRuntime().exec(commands);
            TimeUnit.SECONDS.sleep(5); // wait for command to finish execution
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    
        lastFetch = currentFetch;
    }

    static String durationFormat(Duration d) {
        long nbSecond = d.toSeconds();
        if(nbSecond < 86400 ) {
            return String.format("%dh %02d:%02d",  nbSecond/ 3600, (nbSecond % 3600) / 60, (nbSecond % 60));
        } else {
            return String.format("%djrs %d:%02d:%02d",  nbSecond / 86400, (nbSecond%86400)/ 3600, (nbSecond % 3600) / 60, (nbSecond % 60));
        }
    }
}
