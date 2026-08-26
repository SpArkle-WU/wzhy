package cn.wolfcode.wolf2w.business.test;

import cn.wolfcode.wolf2w.business.domain.Product;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest
class ProductTest {
    @Autowired
    private ElasticsearchClient client;


    @Test
    void 查询价格为15299的商品() throws IOException {
        SearchResponse<Product> resp = client.search(s -> s.index("product")
                .query(q -> q.term(
                        t -> t.field("price").value(15299)
                )), Product.class);
        HitsMetadata<Product> hits = resp.hits();
        long total = hits.total().value();
        System.err.println(total);
        List<Hit<Product>> hits2 = hits.hits();
        for (Hit<Product> hit : hits2) {
            Product product = hit.source();
            System.err.println(product);
            String id = hit.id();
            System.err.println(id);
        }
    }

    @Test
    void 查询价格在2000到5000的商品() throws IOException {
        SearchResponse<Product> resp = client.search(s -> s.index("product")
                .query(q->q.range(
                        r->r.number(
                                n->n.field("price").gt(2000d).lt(5000d)
                ))), Product.class);
        HitsMetadata<Product> hits = resp.hits();
        long total = hits.total().value();
        System.err.println(total);
        List<Hit<Product>> hits2 = hits.hits();
        for (Hit<Product> hit : hits2) {
            Product product = hit.source();
            System.err.println(product);
            String id = hit.id();
            System.err.println(id);
        }
    }

    @Test
    void 查询标题或简介中有蓝牙指纹双卡的商品() throws IOException {
        SearchResponse<Product> resp = client.search(s -> s.index("product")
                .query(q ->q.multiMatch(m->m.query("蓝牙指纹双卡").fields("title","intro")
                )), Product.class);
        HitsMetadata<Product> hits = resp.hits();
        long total = hits.total().value();
        System.err.println(total);
        List<Hit<Product>> hits2 = hits.hits();
        for (Hit<Product> hit : hits2) {
            Product product = hit.source();
            System.err.println(product);
            String id = hit.id();
            System.err.println(id);
        }
    }


    @Test
    void 查询标题含有游戏手机字样的商品() throws IOException {
        SearchResponse<Product> resp = client.search(s -> s.index("product")
                .query(q->q.match(m->m.query("游戏手机").field("title")
                )), Product.class);
        HitsMetadata<Product> hits = resp.hits();
        long total = hits.total().value();
        System.err.println(total);
        List<Hit<Product>> hits2 = hits.hits();
        for (Hit<Product> hit : hits2) {
            Product product = hit.source();
            System.err.println(product);
            String id = hit.id();
            System.err.println(id);
        }
    }


    @Test
    void 查询标题含有pro字样并且价格小于5000的商品() throws IOException {
        SearchResponse<Product> resp = client.search(s -> s.index("product")
                .query(q->q.bool(b->b.must(
                        m1->m1.match(m11->m11.field("title").query("pro"))
                ).must(m2->m2.range(r->r.number(n->n.field("price").lt(5000d))))
                )), Product.class);
        HitsMetadata<Product> hits = resp.hits();
        long total = hits.total().value();
        System.err.println(total);
        List<Hit<Product>> hits2 = hits.hits();
        for (Hit<Product> hit : hits2) {
            Product product = hit.source();
            System.err.println(product);
            String id = hit.id();
            System.err.println(id);
        }
    }


    @Test
    void 查询标题含有pro字样并且价格小于5000的商品价格升序分页展示() throws IOException {
        SearchResponse<Product> resp = client.search(s -> s.index("product")
                .sort(st->st.field(FieldSort.of(o->o.field("price").order(SortOrder.Asc))))
                .from(0).size(3)
                .query(q->q.bool(b->b.must(
                                m1->m1.match(m11->m11.field("title").query("pro"))
                        ).must(m2->m2.range(r->r.number(n->n.field("price").lt(25000d))))
                )), Product.class);
        HitsMetadata<Product> hits = resp.hits();
        long total = hits.total().value();
        System.err.println(total);
        List<Hit<Product>> hits2 = hits.hits();
        for (Hit<Product> hit : hits2) {
            Product product = hit.source();
            System.err.println(product);
            String id = hit.id();
            System.err.println(id);
        }
    }
}












