// 서버 - org.example.TMXCollisionParser.java
package org.example;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.util.*;

/**
 * TMX 파일을 파싱해서 충돌 맵 생성
 * 클라이언트 GameMap.java와 동일한 충돌 레이어 사용:
 * - wall 그룹의 wall 레이어
 * - item 그룹의 furniture1, furniture2, furniture3 레이어
 */
public class TMXCollisionParser {

    public static CollisionMap parse(String tmxPath) {
        try {
            System.out.println("[TMX Parser] TMX 파싱 시작: " + tmxPath);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(tmxPath));

            Element mapElement = doc.getDocumentElement();
            int width = Integer.parseInt(mapElement.getAttribute("width"));
            int height = Integer.parseInt(mapElement.getAttribute("height"));
            int tileWidth = Integer.parseInt(mapElement.getAttribute("tilewidth"));
            int tileHeight = Integer.parseInt(mapElement.getAttribute("tileheight"));

            System.out.println("[TMX Parser] 맵 크기: " + width + "x" + height);
            System.out.println("[TMX Parser] 타일 크기: " + tileWidth + "x" + tileHeight);

            // 초기값: 모두 이동 가능
            boolean[][] walkable = new boolean[height][width];
            for (int y = 0; y < height; y++) {
                Arrays.fill(walkable[y], true);
            }

            // 클라이언트와 동일한 충돌 레이어 정의
            String[][] collisionLayers = {
                {"wall", "wall"},
                {"item", "furniture1"},
                {"item", "furniture2"},
                {"item", "furniture3"}
            };

            // 그룹 레이어 찾기
            NodeList groups = doc.getElementsByTagName("group");
            System.out.println("[TMX Parser] 그룹 개수: " + groups.getLength());

            for (int g = 0; g < groups.getLength(); g++) {
                Element group = (Element) groups.item(g);
                String groupName = group.getAttribute("name");

                // 충돌 대상 그룹인지 확인
                for (String[] layerInfo : collisionLayers) {
                    if (groupName.equals(layerInfo[0])) {
                        String targetLayerName = layerInfo[1];

                        // 그룹 내의 레이어 찾기
                        NodeList layers = group.getElementsByTagName("layer");
                        for (int l = 0; l < layers.getLength(); l++) {
                            Element layer = (Element) layers.item(l);
                            String layerName = layer.getAttribute("name");

                            if (layerName.equals(targetLayerName)) {
                                System.out.println("[TMX Parser] 충돌 레이어 발견: " +
                                    groupName + "/" + layerName);
                                parseTileData(layer, walkable, width, height);
                            }
                        }
                    }
                }
            }

            // 벽 개수 카운트
            int wallCount = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (!walkable[y][x]) wallCount++;
                }
            }

            System.out.println("[TMX Parser] 벽 타일: " + wallCount + " / " + (width * height));
            System.out.println("[TMX Parser] 파싱 완료!");

            return new CollisionMap(walkable, width, height, tileWidth, tileHeight);

        } catch (Exception e) {
            System.err.println("[TMX Parser] 파싱 실패: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 레이어의 타일 데이터를 파싱해서 walkable 배열 업데이트
     */
    private static void parseTileData(Element layer, boolean[][] walkable,
                                      int width, int height) {
        NodeList dataNodes = layer.getElementsByTagName("data");
        if (dataNodes.getLength() == 0) {
            System.err.println("[TMX Parser] data 노드 없음");
            return;
        }

        Element dataElement = (Element) dataNodes.item(0);
        String encoding = dataElement.getAttribute("encoding");

        if ("csv".equals(encoding)) {
            // CSV 인코딩 (가장 흔함)
            String csvData = dataElement.getTextContent().trim();
            String[] rows = csvData.split("\n");

            for (int y = 0; y < rows.length && y < height; y++) {
                String[] tiles = rows[y].trim().split(",");
                for (int x = 0; x < tiles.length && x < width; x++) {
                    long gid = Long.parseLong(tiles[x].trim());
                    int tileId = (int)(gid & 0x0FFFFFFF);  // 하위 28비트만 추출 (실제 타일 ID)
                    if (tileId > 0) {  // 타일이 있으면 벽
                        walkable[y][x] = false;
                    }
                }
            }
        } else if ("base64".equals(encoding)) {
            // Base64 인코딩 (압축된 경우)
            System.err.println("[TMX Parser] Base64 인코딩은 아직 미지원");
            // 필요하면 구현 (java.util.Base64 사용)
        } else {
            // XML 형식 (tile 태그들)
            NodeList tiles = dataElement.getElementsByTagName("tile");
            for (int i = 0; i < tiles.getLength(); i++) {
                Element tile = (Element) tiles.item(i);
                int gid = Integer.parseInt(tile.getAttribute("gid"));

                if (gid > 0) {
                    int x = i % width;
                    int y = i / width;
                    walkable[y][x] = false;
                }
            }
        }
    }
}
