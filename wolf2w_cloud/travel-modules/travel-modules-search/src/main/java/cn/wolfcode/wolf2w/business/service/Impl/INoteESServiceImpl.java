package cn.wolfcode.wolf2w.business.service.Impl;

import cn.wolfcode.wolf2w.business.api.RemoteNoteService;
import cn.wolfcode.wolf2w.business.api.domain.Note;
import cn.wolfcode.wolf2w.business.api.domain.NoteES;
import cn.wolfcode.wolf2w.business.service.INoteESService;
import cn.wolfcode.wolf2w.common.core.domain.R;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class INoteESServiceImpl implements INoteESService {
    @Autowired
    private RemoteNoteService remoteNoteService;
    @Autowired
    private ElasticsearchClient client;

    // 攻略索引名称
    public static final String INDEX_NAME = "note";

    @Override
    public void init() throws IOException, InvocationTargetException, IllegalAccessException {
        System.out.println("初始化ES 数据:数据库->ES");

        // 先读取远程数据，远程服务失败时不要删除已有索引
        R<List<Note>> result = remoteNoteService.list("inner");
        if (result == null || R.isError(result)) {
            throw new IOException("读取游记数据失败: " + (result == null ? "远程服务无响应" : result.getMsg()));
        }
        List<Note> data = result.getData();
        if (data == null) {
            throw new IOException("读取游记数据失败: 远程服务返回空数据");
        }

        // 索引如果存在,删除
        BooleanResponse exists = client.indices().exists(e -> e.index(INDEX_NAME));
        if (exists.value()) {
            client.indices().delete(d -> d.index(INDEX_NAME));
        }

        // 索引映射
        CreateIndexRequest request = CreateIndexRequest.of(r -> r.index(INDEX_NAME)
                .settings(s -> s.numberOfShards("1").numberOfReplicas("1"))
                .mappings(m -> m.properties("id", p -> p.long_(l -> l))
                        .properties("title", p -> p.text(t -> t.analyzer("ik_max_word")))
                        .properties("summary", p -> p.text(t -> t.analyzer("ik_max_word")))
                ));
        // 创建索引
        client.indices().create(request);

        if (data.isEmpty()) {
            System.out.println("游记数据为空，已创建空索引");
            return;
        }

        // 批量插入索引数据
        BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();
        for (Note note : data) {
            NoteES es = new NoteES();
            BeanUtils.copyProperties(es, note);
            bulkBuilder.operations(op -> op
                    .index(idx -> idx
                            .index(INDEX_NAME)
                            .id(String.valueOf(es.getId()))
                            .document(es)
                    )
            );
        }

        BulkResponse bulkResponse = client.bulk(bulkBuilder.refresh(Refresh.WaitFor).build());
        if (bulkResponse.errors()) {
            String errors = bulkResponse.items().stream()
                    .filter(item -> item.error() != null)
                    .map(item -> item.error().reason())
                    .limit(3)
                    .collect(Collectors.joining("; "));
            throw new IOException("批量写入 Elasticsearch 失败: " + errors);
        }
        System.out.println("批量插入完成，共插入 " + data.size() + " 条数据");
    }
}

