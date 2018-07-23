package com.fastfur.messaging.exercise.solution;

import com.fastfur.messaging.data.Tweet;
import com.fastfur.messaging.producer.twitter.TwitterTopics;
import com.fastfur.messaging.serde.TweetSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;
import java.util.concurrent.TimeUnit;


public class JoinTweetsSolution {
    public static void main(String[] args) throws Exception {
        Properties config = new Properties();
        config.put( StreamsConfig.APPLICATION_ID_CONFIG, "my-first-tweet-ks1" );
        config.put( StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092" );
        config.put( StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName() );
        config.put( StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, TweetSerde.class );


        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Tweet> stream = builder.stream( TwitterTopics.TWITTERS_TOPIC, Consumed.with( Serdes.String(), new TweetSerde() ) );
        KStream<String, Tweet> responseStream = builder.stream( TwitterTopics.GOT_RESPONDED_TOPIC, Consumed.with( Serdes.String(), new TweetSerde() ) );

        stream.filter( (k, v) -> v.getInReponseTo() != -1 )
                .selectKey( (k, v) -> String.valueOf( v.getInReponseTo() ) )
                .join( responseStream, (left, right) -> TimeUnit.MILLISECONDS.toSeconds( left.getCreatedAt().getTime() - right.getCreatedAt().getTime() ),
                        JoinWindows.of( 300000 ).before( 600000 ).until( 3600000 ) )
                .foreach( (k, v) -> System.out.println( "key : " + k.toString() + " value : " + v ) );


        KafkaStreams streams = new KafkaStreams( builder.build(), config );
        streams.start();
    }
}
