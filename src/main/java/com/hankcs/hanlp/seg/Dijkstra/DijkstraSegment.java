/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/10/29 15:14</create-date>
 *
 * <copyright file="Segment.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.seg.Dijkstra;

import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.recognition.nr.JapanesePersonRecognition;
import com.hankcs.hanlp.recognition.nr.PersonRecognition;
import com.hankcs.hanlp.recognition.nr.TranslatedPersonRecognition;
import com.hankcs.hanlp.recognition.ns.PlaceRecognition;
import com.hankcs.hanlp.recognition.nt.OrganizationRecognition;
import com.hankcs.hanlp.seg.Dijkstra.Path.State;
import com.hankcs.hanlp.seg.WordBasedGenerativeModelSegment;
import com.hankcs.hanlp.seg.common.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * 最短路径分词
 *
 * @author hankcs
 */
public class DijkstraSegment extends WordBasedGenerativeModelSegment {
    @Override
    public List<Term> segSentence(char[] sentence) {
        WordNet wordNetOptimum = new WordNet(sentence);
        WordNet wordNetAll = new WordNet(wordNetOptimum.charArray);

        GenerateWordNet(wordNetAll);
        Graph graph = GenerateBiGraph(wordNetAll);

        List<Vertex> vertexList = dijkstra(graph);
        if (config.useCustomDictionary) {
            if (config.indexMode) {
                CustomDictionary.INSTANCE.combineByCustomDictionary(vertexList, wordNetAll);
            }
            else {
                CustomDictionary.INSTANCE.combineByCustomDictionary(vertexList);
            }
        }


        // 数字识别
        if (config.numberQuantifierRecognize) {
            mergeNumberQuantifier(vertexList, wordNetAll, config);
        }

        // 实体命名识别
        if (config.ner) {
            wordNetOptimum.addAll(vertexList);
            int preSize = wordNetOptimum.size();
            if (config.nameRecognize) {
                PersonRecognition.Recognition(vertexList, wordNetOptimum, wordNetAll);
            }
            if (config.translatedNameRecognize) {
                TranslatedPersonRecognition.Recognition(vertexList, wordNetOptimum, wordNetAll);
            }
            if (config.japaneseNameRecognize) {
                JapanesePersonRecognition.Recognition(vertexList, wordNetOptimum, wordNetAll);
            }
            if (config.placeRecognize) {
                PlaceRecognition.Recognition(vertexList, wordNetOptimum, wordNetAll);
            }
            if (config.organizationRecognize) {
                // 层叠隐马模型——生成输出作为下一级隐马输入
                graph = GenerateBiGraph(wordNetOptimum);
                vertexList = dijkstra(graph);
                wordNetOptimum.clear();
                wordNetOptimum.addAll(vertexList);
                preSize = wordNetOptimum.size();
                OrganizationRecognition.Recognition(vertexList, wordNetOptimum, wordNetAll);
            }
            if (wordNetOptimum.size() != preSize) {
                graph = GenerateBiGraph(wordNetOptimum);
                vertexList = dijkstra(graph);
            }
        }

        // 如果是索引模式则全切分
        if (config.indexMode) {
            return decorateResultForIndexMode(vertexList, wordNetAll);
        }

        // 是否标注词性
        if (config.speechTagging) {
            speechTagging(vertexList);
        }

        return convert(vertexList, config.offset);
    }

    /**
     * dijkstra最短路径
     *
     * @param graph
     * @return
     */
    private static List<Vertex> dijkstra(Graph graph) {
        List<Vertex> resultList = new LinkedList<Vertex>();
        Vertex[] vertexes = graph.getVertexes();
        List<EdgeFrom>[] edgesTo = graph.getEdgesTo();
        double[] d = new double[vertexes.length];
        Arrays.fill(d, Double.MAX_VALUE);
        d[d.length - 1] = 0;
        int[] path = new int[vertexes.length];
        Arrays.fill(path, -1);
        PriorityQueue<State> que = new PriorityQueue<State>();
        que.add(new State(0, vertexes.length - 1));
        while (!que.isEmpty()) {
            State p = que.poll();
            if (d[p.vertex] < p.cost) continue;
            for (EdgeFrom edgeFrom : edgesTo[p.vertex]) {
                if (d[edgeFrom.from] > d[p.vertex] + edgeFrom.weight) {
                    d[edgeFrom.from] = d[p.vertex] + edgeFrom.weight;
                    que.add(new State(d[edgeFrom.from], edgeFrom.from));
                    path[edgeFrom.from] = p.vertex;
                }
            }
        }
        for (int t = 0; t != -1; t = path[t]) {
            resultList.add(vertexes[t]);
        }
        return resultList;
    }

}
