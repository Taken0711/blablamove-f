package fr.unice.polytech.al.teamf;

import fr.unice.polytech.al.teamf.chaosmonkey.ChaosMonkey;
import fr.unice.polytech.al.teamf.entities.GPSCoordinate;
import fr.unice.polytech.al.teamf.entities.Mission;
import fr.unice.polytech.al.teamf.entities.Parcel;
import fr.unice.polytech.al.teamf.entities.User;
import fr.unice.polytech.al.teamf.message_listeners.MessageReceiver;
import fr.unice.polytech.al.teamf.repositories.MissionRepository;
import fr.unice.polytech.al.teamf.repositories.ParcelRepository;
import fr.unice.polytech.al.teamf.repositories.UserRepository;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.transaction.Transactional;

@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    UserRepository userRepository;
    @Autowired
    ParcelRepository parcelRepository;
    @Autowired
    MissionRepository missionRepository;

//    static final String sendingTopicExchangeName = "external-sending-exchange";

//    static final String sendingQueueName = "external-sending";

    static final String pointpricingTopicExchangeName = "pointpricing-receiving-exchange";

    static final String pointpricingQueueName = "pointpricing-receiving";

    static final String routefindingTopicExchangeName = "routefinding-receiving-exchange";

    static final String routefindingQueueName = "routefinding-receiving";

    static final String insuranceTopicExchangeName = "insurance-receiving-exchange";

    static final String insuranceQueueName = "insurance-receiving";

    @Value("${chaos_monkey_address}")
    public String chaos_monkey_url;

//    @Bean
//    Queue sendingQueue() {
//        return new Queue(sendingQueueName, false);
//    }

    @Bean
    Queue pointPricingQueue() {
        return new Queue(pointpricingQueueName, false);
    }

    @Bean
    Queue routeFindingQueue() {
        return new Queue(routefindingQueueName, false);
    }

    @Bean
    Queue insuranceQueue() {
        return new Queue(insuranceQueueName, false);
    }

//    @Bean
//    TopicExchange sendingExchange() {
//        return new TopicExchange(sendingTopicExchangeName);
//    }

    @Bean
    TopicExchange pointPricingExchange() {
        return new TopicExchange(pointpricingTopicExchangeName);
    }

    @Bean
    TopicExchange routeFindingExchange() {
        return new TopicExchange(routefindingTopicExchangeName);
    }

    @Bean
    TopicExchange insuranceExchange() {
        return new TopicExchange(insuranceTopicExchangeName);
    }

//    @Bean
//    Binding sendingBinding() {
//        return BindingBuilder.bind(sendingQueue()).to(sendingExchange()).with("external.contact.#");
//    }

    @Bean
    Binding pointPricingBinding() {
        return BindingBuilder.bind(pointPricingQueue()).to(pointPricingExchange()).with("external.pointpricing.#");
    }

    @Bean
    Binding routeFindingBinding() {
        return BindingBuilder.bind(routeFindingQueue()).to(routeFindingExchange()).with("external.routefinder.#");
    }

    @Bean
    Binding insuranceBinding() {
        return BindingBuilder.bind(insuranceQueue()).to(insuranceExchange()).with("external.insurance.#");
    }

    @Bean
    SimpleMessageListenerContainer pointPricingContainer(ConnectionFactory connectionFactory, MessageReceiver receiver) {
        return getSimpleMessageListenerContainer(connectionFactory, pointPricingListenerAdapter(receiver), pointpricingQueueName);
    }

    @Bean
    SimpleMessageListenerContainer routeFindingContainer(ConnectionFactory connectionFactory, MessageReceiver receiver) {
        return getSimpleMessageListenerContainer(connectionFactory, routeFindingListenerAdapter(receiver), routefindingQueueName);
    }

    @Bean
    SimpleMessageListenerContainer insuranceContainer(ConnectionFactory connectionFactory, MessageReceiver receiver) {
        return getSimpleMessageListenerContainer(connectionFactory, insuranceListenerAdapter(receiver), insuranceQueueName);
    }

    private SimpleMessageListenerContainer getSimpleMessageListenerContainer(ConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter, String queueName) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName);
        container.setMessageListener(listenerAdapter);
//        container.setChannelAwareMessageListener();
        return container;
    }

    @Bean
    MessageListenerAdapter pointPricingListenerAdapter(MessageReceiver receiver) {
        return new MessageListenerAdapter(receiver, "receivePointPricingMessage");
    }

    @Bean
    MessageListenerAdapter routeFindingListenerAdapter(MessageReceiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveRouteFindingMessage");
    }

    @Bean
    MessageListenerAdapter insuranceListenerAdapter(MessageReceiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveInsuranceMessage");
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    @Transactional
    public void run(String... arg0) throws Exception {
        ChaosMonkey.getInstance().initialize(chaos_monkey_url + "/settings");

        User thomas = new User("Thomas");
        userRepository.save(thomas);
        User loic = new User("Loic");
        userRepository.save(loic);
        User jeremy = new User("Jeremy");
        userRepository.save(jeremy);
        User johann = new User("Johann");
        userRepository.save(johann);
        User erick = new User("Erick");
        userRepository.save(erick);
        User julien = new User("Julien");
        userRepository.save(julien);

        Parcel parcel1 = new Parcel(jeremy);
        parcelRepository.save(parcel1);
        Mission jeremysMission = new Mission(johann, jeremy, new GPSCoordinate(10, 12), new GPSCoordinate(10, 42), parcel1);
        jeremysMission.setOngoing();
        parcel1.setMission(jeremysMission);
        missionRepository.save(jeremysMission);
        johann.addTransportedMission(jeremysMission);

        Parcel parcel2 = new Parcel(thomas);
        parcelRepository.save(parcel2);
        Mission thomasMission = new Mission(johann, thomas, new GPSCoordinate(10, 12), new GPSCoordinate(10, 69), parcel2);
        thomasMission.setOngoing();
        parcel2.setMission(thomasMission);
        missionRepository.save(thomasMission);
        johann.addTransportedMission(thomasMission);

    }

}
