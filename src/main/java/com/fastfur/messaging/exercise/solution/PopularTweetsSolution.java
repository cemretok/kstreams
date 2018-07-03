package com.fastfur.messaging.exercise.solution;

import com.fastfur.messaging.data.Tweet;
import com.fastfur.messaging.producer.twitter.TwitterTopics;
import com.fastfur.messaging.serde.TweetSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;

import java.util.Properties;

public class PopularTweetsSolution {
    public static void main(String[] args) throws Exception {


        Properties config = new Properties();
        config.put( StreamsConfig.APPLICATION_ID_CONFIG, "my-first-tweet-ks1" );
        config.put( StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092" );
        config.put( StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName() );
        config.put( StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, TweetSerde.class );


        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Tweet> stream = builder.stream( TwitterTopics.TWITTERS_TOPIC, Consumed.with( Serdes.String(), new TweetSerde() ) );
        KTable<Windowed<String>, Tweet> longSums =
                stream.filter( (k, v) -> v.getFavoriteCount() > 10 )
                        .groupBy( (k, v) -> v.getLanguage() )
                        .windowedBy( TimeWindows.of( 60000L ).until( 60000L * 10 ) )
                        .reduce( (v1, v2) -> v1.getFavoriteCount() > v2.getFavoriteCount() ? v1 : v2 );
        longSums.foreach( (k, v) -> System.out.println( "start -> " + k.window().start() + "  key -> " + k.key() + " value ->" + v.toString() )) ;

        KafkaStreams streams = new KafkaStreams( builder.build(), config );
        streams.start();


    }
}
