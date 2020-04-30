package eg.edu.alexu.csd.filestructure.btree;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.management.RuntimeErrorException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.lang.reflect.Array;
import java.util.*;

public class SearchEngine implements ISearchEngine {

    private IBTree<String,List <ISearchResult>> iBTree;

    SearchEngine(int t) {
        iBTree = new BTree<>(t);
    }

    private void buildBTree(IBTree<String,List<ISearchResult>> bTree,String filePath) {

        throwException(filePath);
        filePath = filePath.replace("\\",File.separator);

        try {

            File inputFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("doc");

            for (int i = 0; i < nodeList.getLength(); i++) {

                Element node = (Element) nodeList.item(i);

                String id = node.getAttribute("id"); String arr[] = node.getTextContent().toLowerCase().split("\\s+");

                Hashtable<String,Integer> hashtable = new Hashtable<>();

                for (int j = 1; j < arr.length; j++) {

                    int temp;
                    if (hashtable.containsKey(arr[j])) {
                        temp = hashtable.get(arr[j]) + 1;
                    } else {
                        temp = 1;
                    }
                    hashtable.put(arr[j],temp);

                }

                Iterator<Map.Entry<String,Integer>> iterator = hashtable.entrySet().iterator();

                while (iterator.hasNext()) {

                    Map.Entry<String,Integer> temp = iterator.next();
                    List<ISearchResult> list = bTree.search(temp.getKey());

                    if (list == null) {
                        list = new ArrayList<>(); bTree.insert(temp.getKey(),list);
                    }

                    list.add(new SearchResult(id,temp.getValue()));

                }

            }

        }catch (Exception e) {
            throwException(null);
        }

    }

    private void throwException(Object o) {
        if (o == null) throw new RuntimeErrorException(null);
    }

    @Override
    public void indexWebPage(String filePath) {
        buildBTree(iBTree,filePath);
    }

    @Override
    public void indexDirectory(String directoryPath) {
        throwException(directoryPath);
        File folder = new File(directoryPath);
        if (folder.isDirectory()) {
            listFilesForFolder(folder);
        } else {
            throwException(null);
        }
    }

    private void listFilesForFolder(File folder) {

        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                listFilesForFolder(file);
            } else {
                indexWebPage(file.getPath());
            }
        }

    }

    private void traverse(IBTreeNode<String,List<ISearchResult>> node) {

        for (int i = 0; i < node.getNumOfKeys(); i++) {

            List<ISearchResult> list1 = iBTree.search(node.getKeys().get(i));

            if (list1 == null) continue; int k = 0,j = 0;

            List<ISearchResult> list2 = node.getValues().get(i); List<ISearchResult> newList = new ArrayList<>();
            sort(list1); sort(list2);
            while (j < list1.size() && k < list2.size()) {
                int compare = list1.get(j).getId().compareTo(list2.get(k).getId());
                if (compare == 0) {
                    k++; j++;
                } else if (compare > 0) {
                    k++;
                } else {
                    newList.add(list1.get(j)); j++;
                }
            }

            for (int l = j; l < list1.size(); l++) {
                newList.add(list1.get(l));
            }

            list1.clear(); list1.addAll(newList);

            if (list1.size() == 0) {
                iBTree.delete(node.getKeys().get(i));
            }

        }

        if (!node.isLeaf()) for (int i = 0; i < node.getChildren().size(); i++) {
            traverse(node.getChildren().get(i));
        }

    }

    @Override
    public void deleteWebPage(String filePath) {
        IBTree<String,List<ISearchResult>> bTree = new BTree<>(100);
        buildBTree(bTree,filePath);
        traverse(bTree.getRoot());
    }

    @Override
    public List<ISearchResult> searchByWordWithRanking(String word) {
        throwException(word);
        List <ISearchResult> list;
        if ((list = iBTree.search(word.toLowerCase())) == null) list = new ArrayList<>();
        return list;
    }

    @Override
    public List<ISearchResult> searchByMultipleWordWithRanking(String sentence) {
        throwException(sentence);
        String arr[] = sentence.split("\\s+");
        List<ISearchResult> list = new ArrayList<>();
        int k = 0;
        while (k < arr.length && arr[k].compareTo("") == 0) k++;

        if (k < arr.length) list = searchByWordWithRanking(arr[k++]);

        for (int i = k; i < arr.length; i++) {

            List<ISearchResult> list1 = searchByWordWithRanking(arr[i]);

            if (list == null || arr[i].compareTo("") == 0) continue;

            list = intersection(list,list1);

        }
        return list;
    }

    private void sort(List<ISearchResult> list) {

        Collections.sort(list, new Comparator<ISearchResult>() {
            @Override
            public int compare(ISearchResult o1, ISearchResult o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });

    }

    private List<ISearchResult> intersection(List<ISearchResult> list1, List<ISearchResult> list2) {

        sort(list1); sort(list2); int i = 0,k = 0; List<ISearchResult> newList = new ArrayList<>();

        while (i < list1.size() && k <list2.size()) {
            int compare = list1.get(i).getId().compareTo(list2.get(k).getId());
            if (compare == 0) {
                if (list1.get(i).getRank() - list2.get(k).getRank() < 0) {
                    newList.add(list1.get(i));
                }else {
                    newList.add(list2.get(k));
                }
                    k++;  i++;
            } else if (compare > 0) {
                k++;
            } else {
                i++;
            }
        }

        return newList;
    }
}
