
package com.fastfur.messaging.producer.stocks;

import com.fastfur.messaging.producer.BaseProducer;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.Optional;
import java.util.Properties;

/**
 * This is class (kafka producer) sends the stock value to the kafka topic.
 * The stock values is retrieved by calling https://www.alphavantage.co API
 */
public class VantageProducer extends BaseProducer {

    private static final String VANTAGE_ORIGINAL_TOPIC = "vantage";
    private VantageConnector vgConn;

    public VantageProducer() {
        initProps();
        producer = new KafkaProducer(properties);
        vgConn = new VantageConnector();
    }

    public void produceStockValues(String stock) throws Exception {

        String jsonData = vgConn.getData("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" + stock + "&outputsize=full");
        Gson gson = new Gson();

        Optional <JsonObject> timeSeries =
                Optional.ofNullable(gson.fromJson(jsonData, JsonElement.class).getAsJsonObject().get("Time Series (Daily)").getAsJsonObject());

        timeSeries.ifPresent(j-> j.entrySet().stream().map(v -> {
            DailyStockData dailyStockData = gson.fromJson(v.getValue(), DailyStockData.class);
            dailyStockData.setDay(v.getKey());
            return dailyStockData;
        }). //forEach(v -> System.out.println(v)));
                forEach(v -> produce(v, VANTAGE_ORIGINAL_TOPIC)));
    }

    public Properties initProps() {
        super.initProps();
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "com.fastfur.messaging.serde.StockSerializer");
        return properties;
    }

    public static void main(String[] args) throws Exception {
        VantageProducer vantageProducer = new VantageProducer();
        vantageProducer.produceStockValues("LPSN");
    }
}
