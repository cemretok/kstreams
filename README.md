# Kafka Streams workshop
## Prerequisites 
1. Install docker, CE (Community Edition). The installation instructions for Linux, Mac and Windows you can find [here](https://docs.docker.com/install/)
2. Install docker compose [here](https://docs.docker.com/compose/install/).
3. Verify the installation. Run *docker* , *docker-compose* commands from your terminal
4. In our workshop we're going to use Twitter API for the real-time data. So everyone should generate API keys and token to access the API.

   Go to this [link](https://apps.twitter.com/app/new) (You need to be logged into your twitter account) Follow the instructions. 
   
   - In *Website* tab you can put any site URL, e.g. https://developer.twitter.com/
   - In *Callback URLs* click on *Add a Callback URL* button
   - Click on *Generate your Twitter application* button
   - Go to the *Keys and Access Tokens* tab and click on *Create my access tocken* button
   
   After this process you supposed to have 4 keys: 
   + API KEY (Consumer key)
   + API secret (Consumer secret)
   + Access token
   + Access token secret
   
   Save them in safe place on your laptop. We'll use all of them later on.
   
 5. In addition to Twitter API we'll use [this API](https://www.alphavantage.co/) for real-time stock data
 
    Open this link above and click on *Get your free API key today*. 
    
    Follow the instructions and get the key and save it.
    
    
 ### Docker Compose
 In this workshop we'll use docker-compose to create our dockerized development environment.
 
 docker-compose will start all required containers for this workshop: Kafka and Zookeeper conatiners.
 
 + git clone this repo
 + cd to  *./docker* folder
 + *docker-compose up -d*
 + check the docker are up and running : *docker ps*
 
 + In order to get into Kafka container, run :
   *docker exec -i -t container-id /bin/bash*
 + Check if all Kafka topics have been created properly :
 
       docker ps
       docker exec -i -t zk-container-id /bin/bash
       ifconfig  (take ZK IP)
       docker exec -i -t kafka-container-id /bin/bash
       $KAFKA_HOME/bin/kafka-topics.sh --list --zookeeper zk-IP
       
       E.g. : $KAFKA_HOME/bin/kafka-topics.sh --list --zookeeper 172.18.0.2:2181
       
  It should print list of topics like that : 
  
       outputtopic1
       outputtopic2
       outputtopic3
       twitters
   
 Looks like we're ready to go...
 
 
 ### Kafka consumer
 We need to start Kafka consumers for the topics of our interest.
 
 To do that run, inside of kafka container (this is the input topic where the tweets come to): 
      
      $KAFKA_HOME/bin/kafka-console-consumer.sh --bootstrap-server 172.18.0.3:9092 --topic twitters --from-beginning
      
 When we start Kafka Streams topology we supposed to see here a stream of incoming data (tweets) to this topic.
 
 Open a second terminal, get into Kafka container and run Kafka console consumer
 
 This is the output topic where we write to the processed by the topology original stream 
 
 In order to print properly the topology output, start the Kafka consumer this way:
 
      $KAFKA_HOME/bin/kafka-console-consumer.sh --bootstrap-server 172.18.0.3:9092 \
            --topic outputtopic2 \
            --formatter kafka.tools.DefaultMessageFormatter \
            --property print.key=true \
            --property print.value=true \
            --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
            --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
          
    
    
    
    
   ### Troubleshooting 
   
   Sometimes stream is stuck (data is not streaming) to resolve it try to run:
   
      docker-compose stop
      docker-compose rm
      
   if the problem persists try to delete all images :
   
      docker stop $(docker ps -a -q)
      docker rm $(docker ps -a -q)
      docker rmi $(docker ps -a -q)
       
   And then rerun docker-compose.
   
   ## Scaling Kafka Streams
    
   In order to scale out Kafka Streams application we'll start a few instances of the same topology.
    
   Every Kafka Streams instance is going to read from its dedicated topic partition. 
    
   (We've created 3 partitions of vantage_input topic. See in docker-compose.yml)
 
 
 
 
  ________________________


 ## Exercises
 See exercises and solutions at *../kstreams/src/main/java/com/fastfur/messaging/exercise*
 In this section we have added relevant documentation and hints for each exercise


   1. **Level: Easy - TrumpTopology. Producer - TweetProducer.** In this exercise you will have to create new stream contain only              tweets that contains 'trump' in their user name . The new stream key will be the user name.
            
   **Hints:** 
   
   + [KStream](https://kafka.apache.org/10/javadoc/org/apache/kafka/streams/kstream/KStream.html)
    
  2. **Level : Easy - EncryptTweet. Producer - TweetWithResponseProducer**
   In this exercise you will have to encrypt tweets(text field only) from two topics :
   tweets & got_responded, and then stream it. After the transformation,
   push the result to the encode_tweets topic.
   For your convenience you can use  the CryptoUtil Class.
   
   **Hints:** 
     useful transformations and tools may be found here :
     
   + [KStream](https://kafka.apache.org/0110/javadoc/org/apache/kafka/streams/kstream/KStream.html)
   + [KStreamBuilder](https://kafka.apache.org/0102/javadoc/org/apache/kafka/streams/kstream/KStreamBuilder.html)
   
   
   3. **Level: Easy - DevicesTopology. Producer - TweetProducer.** 
   In this exercise you will have to count number of tweets created by  each device type(Iphone, Android etc..).
        
   **Hints:** 
   + [KGroupedStream](https://kafka.apache.org/0110/javadoc/org/apache/kafka/streams/kstream/KGroupedStream.html)
   + [KStream](https://kafka.apache.org/10/javadoc/org/apache/kafka/streams/kstream/KStream.html)

   4. **Level: Medium- BranchTopology. Producer -  TweetProducer**
    In this Exercise you will have to Listens to tweet,
    filter only english tweets, branch by predicates to different 
    topics by device and print it 
    
   **Hints:** 
   + [KStream](https://kafka.apache.org/10/javadoc/org/apache/kafka/streams/kstream/KStream.html)
        
  5. **level : Hard - PopularTweets. Producer -  TweetProducer** 
   In this exercise you will have to implement a topology that will print the most popular
   tweet in each minute for each language. The time window should be for the last 10 minutes.
   Filter the tweets such that only tweets with 10 likes and above are passed
   
   **Hints:**
   + [TimeWindows](https://kafka.apache.org/0110/javadoc/org/apache/kafka/streams/kstream/TimeWindows.html)
   + [KGroupedStream](https://kafka.apache.org/0102/javadoc/org/apache/kafka/streams/kstream/KGroupedStream.html)
  
  6. **Level: Hard - JoinTweets. Producer - TweetWithResponseProducer**
   In this exercise you will have to calculate the time difference 
   between a response to a tweet and the original tweet. To achieve this we should join two streams. 
   When you join two sreams, both streams must have the same key. 
   Regarding this specific example the key we use is `id` field in `com.fastfur.messaging.data.Tweet` class. 
   When the people response to the specific tweet - the tweet field `inReponseTo` contains the id of the original tweet (if inReponseTo = -1 then this is the original tweet rather than response to the tweet).
   So the idea is to take all the tweets with inReponseTo != -1 and restream it to the dedicated topic of `responses` with the key = inReponseTo.
   Then join two streams : one from the original topic (the original tweets only!) and other from the topic of `responses`.
    
   **Hints:**
   + [joins](https://docs.confluent.io/current/streams/concepts.html#joins)
   + [KStream](https://kafka.apache.org/0110/javadoc/org/apache/kafka/streams/kstream/KStream.html)
    
  
        
        
        
  
        

            
            
    

     
    
        
     
      
      
      
      
      
      
      
      
         
      
   
 
 
 
 
     
   
