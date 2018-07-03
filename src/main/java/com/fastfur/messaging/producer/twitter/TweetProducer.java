package com.fastfur.messaging.producer.twitter;

import com.fastfur.messaging.data.Tweet;
import com.fastfur.messaging.producer.BaseProducer;
import com.fastfur.messaging.producer.Queries;
import org.apache.kafka.clients.producer.KafkaProducer;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.List;
import java.util.Properties;

public class TweetProducer extends BaseProducer {


    protected Twitter twitter;

    public TweetProducer() {
        TwitterFactory tf = init();
        this.twitter = tf.getInstance();
        initProps();
        producer = new KafkaProducer( properties );
    }

    public void produceTweets(String topic, List<String> queries) throws Exception {
        for (String query : queries) {
            for (Status status : searchTwitts( query )) {
                produce( new Tweet( String.valueOf( status.getId() ), status ), topic );
            }
        }
    }

    public List<Status> searchTwitts(String queryString) throws Exception {
        Query query = new Query( queryString );
        query.setCount( 100 );
        QueryResult result = twitter.search( query );
        return result.getTweets();
    }

    public static void main(String[] args) throws Exception {
        TweetProducer tp = new TweetProducer();
        tp.produceTweets( TwitterTopics.TWITTERS_TOPIC, Queries.getQueries() );

    }

    public TwitterFactory init() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled( true )
                .setOAuthConsumerKey( "syLHmMpyVbhkMEq4ua0xqTEEW" )
                .setOAuthConsumerSecret( "rWbfUOtAT3osmF1ACcJ4gs4AO3NuLPop9PNvdtbv4CZHW3Ccij" )
                .setOAuthAccessToken( "4537889248-WFl9VLppfc30BxKfjF9BY8y7kVXPuCRp4tSIHGD" )
                .setOAuthAccessTokenSecret( "N4lWvSFfO2hMdNK1NpzgT6q3GUEd6ai28zz938QxYs5kX" );
        TwitterFactory tf = new TwitterFactory( cb.build() );
        return tf;
    }

    public Properties initProps() {
        super.initProps();
        properties.put( "key.serializer", "org.apache.kafka.common.serialization.StringSerializer" );
        properties.put( "value.serializer", "com.fastfur.messaging.serde.TweetSerializer" );
        return properties;
    }

}
