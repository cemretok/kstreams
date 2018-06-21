
package com.fastfur.messaging.producer;

import com.fastfur.messaging.data.Tweet;
import org.apache.kafka.clients.producer.KafkaProducer;
import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;


import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by yaarr on 6/19/18.
 */

public class TwitterStreamProducer  extends BaseProducer {

    private final String consumerKey;
    private final String consumerSecret;
    private final String token;
    private final String secret;
    private final String topic;
    private final ArrayList terms;

    public TwitterStreamProducer(String consumerKey,
                                 String consumerSecret,
                                 String token,
                                 String secret,
                                 String topic,
                                 ArrayList terms) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.token = token;
        this.secret = secret;
        this.topic = topic;
        this.terms = terms;
        initProps();
        producer = new KafkaProducer(properties);
    }

    public void run() throws InterruptedException {

        BlockingQueue<String> queue = new LinkedBlockingQueue<String>(10000);
        StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();
        // add some track terms
        endpoint.trackTerms(terms);

        Authentication auth = new OAuth1(consumerKey, consumerSecret, token, secret);
        // Authentication auth = new BasicAuth(username, password);

        // Create a new BasicClient. By default gzip is enabled.
        Client client = new ClientBuilder()
                .hosts(Constants.STREAM_HOST)
                .endpoint(endpoint)
                .authentication(auth)
                .processor(new StringDelimitedProcessor(queue))
                .build();

        // Establish a connection
        client.connect();

        // Do whatever needs to be done with messages
        for (int msgRead = 0; msgRead < 1000; msgRead++) {
            String tweetJson = queue.take();
            //System.out.println(tweetJson);
            produce( new Tweet(tweetJson), topic );
        }

        client.stop();
        producer.close();
    }

    public Properties initProps(){
        super.initProps();
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "com.fastfur.messaging.serde.TweetSerializer");
        return properties;

    }

  public static void main(String[] args) {
    try {
        TwitterStreamProducer twitterStreamProducer = new TwitterStreamProducer(
                "syLHmMpyVbhkMEq4ua0xqTEEW",
                "rWbfUOtAT3osmF1ACcJ4gs4AO3NuLPop9PNvdtbv4CZHW3Ccij",
                "4537889248-WFl9VLppfc30BxKfjF9BY8y7kVXPuCRp4tSIHGD",
                "N4lWvSFfO2hMdNK1NpzgT6q3GUEd6ai28zz938QxYs5kX",
                "twitters",
                Lists.newArrayList("WorldCup", "#WorldCup2018", "Russia")
        );
        twitterStreamProducer.run();
    } catch (InterruptedException e) {
      System.out.println(e);
    }
  }

}
