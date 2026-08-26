package cn.wolfcode.wolf2w.business.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EsConfig {

    @Value("${elasticsearch.url}")
    private String esUrl;
    @Value("${elasticsearch.port}")
    private Integer esPort;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        // 创建RestClient
        RestClient restClient = RestClient.builder(
                new HttpHost(esUrl, esPort, "http")
                        ).build();

        // 创建RestClientTransport
        RestClientTransport transport = new RestClientTransport(restClient,new JacksonJsonpMapper());
        // 创建ElasticsearchAsyncClient
        return new ElasticsearchClient(transport);
    }
}
