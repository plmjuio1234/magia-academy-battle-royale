package com.example.yugeup.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.example.yugeup.utils.AssetManager;
import com.example.yugeup.utils.Constants;
import com.example.yugeup.utils.Logger;
import com.example.yugeup.utils.ScreenTransition;

/**
 * 로딩 화면
 *
 * 게임 시작 시 에셋을 로드하고 진행률을 표시합니다.
 * 로딩 완료 후 메인 메뉴로 전환됩니다.
 *
 * @author YuGeup Development Team
 * @version 1.0
 */
public class LoadingScreen implements Screen {

    private Game game;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    // Viewport (화면 비율 유지)
    private Viewport viewport;
    private OrthographicCamera camera;

    // 로딩 배경 (먼저 로드해야 함)
    private Texture loadingBackground;

    // 로딩 화면용 폰트 (미리 로드)
    private BitmapFont loadingFont;
    private BitmapFont loadingFontLarge;

    // 에셋 매니저
    private AssetManager assetManager;

    // 로딩 상태
    private boolean assetsLoaded;
    private float loadingTime;
    private String tipText;

    // 화면 전환
    private ScreenTransition transition;
    private boolean transitionStarted;

    // 100% 도달 후 추가 대기 시간
    private float fullProgressTime;
    private static final float WAIT_AFTER_100_PERCENT = 1.0f;  // 100% 후 1초 대기

    // UI 설정 (모바일 게임 스타일)
    private static final float LOADING_BAR_WIDTH = 1600f;  // 화면 대비 적절한 크기
    private static final float LOADING_BAR_HEIGHT = 40f;   // 로딩바 높이
    private static final float LOADING_BAR_Y = 200f;       // 화면 하단에서 200px
    private static final float TIP_Y = 100f;               // 화면 하단에서 100px
    private static final float LOADING_BAR_PADDING = 6f;   // 로딩바 내부 패딩
    private static final float LOADING_BAR_RADIUS = 20f;   // 로딩바 둥근 모서리 반경

    /**
     * LoadingScreen을 생성합니다.
     *
     * @param game 게임 인스턴스
     */
    public LoadingScreen(Game game) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.assetManager = AssetManager.getInstance();
        this.assetsLoaded = false;
        this.loadingTime = 0f;
        this.transitionStarted = false;
        this.fullProgressTime = 0f;

        // 카메라와 뷰포트 설정 (가상 해상도 사용)
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, camera);
        this.camera.position.set(Constants.SCREEN_WIDTH / 2f, Constants.SCREEN_HEIGHT / 2f, 0);

        // 로딩 팁 선택
        this.tipText = Constants.getRandomTip();

        // 화면 전환 초기화
        this.transition = new ScreenTransition();

        Logger.info("로딩 화면 생성됨");
    }

    @Override
    public void show() {
        Logger.info("로딩 화면 표시 시작");

        try {
            // 로딩 화면 배경 먼저 로드
            loadingBackground = new Texture(Gdx.files.internal(Constants.LOADING_BACKGROUND_PATH));
            Logger.info("로딩 배경 이미지 로드 완료");

            // 로딩 화면용 폰트 미리 생성 (한글 지원)
            com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator generator =
                new com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator(
                    Gdx.files.internal(Constants.FONT_REGULAR_PATH)
                );

            com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter parameter =
                new com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter();

            // 한글 문자셋 - 실제 사용할 문자만 나열
            String koreanChars = "가각간갇갈갉감갑값갓강갖갗같갚갛개객갠갤갬갭갯갰갱갸갹갼걀걋걍걔걘걜거걱건걷걸걺검겁것겄겅겆겉겊겋게겐겔겜겝겟겠겡겨격겪견겯결겸겹겻겼경곁계곈곌곕곗고곡곤곧골곪곬곯곰곱곳공곶과곽관괄괆괌괍괏광괘괜괠괩괬괭괴괵괸괼굄굅굇굉교굔굘굡굣구국군굳굴굵굶굻굼굽굿궁궂궈궉권궐궜궝궤궷귀귁귄귈귐귑귓규균귤그극근귿글긁금급긋긍긔기긱긴긷길긺김깁깃깅깆깊까깍깎깐깔깖깜깝깟깠깡깥깨깩깬깰깸깹깻깼깽꺄꺅꺌꺼꺽꺾껀껄껌껍껏껐껑께껙껜껨껫껭껴껸껼꼇꼈꼍꼐꼬꼭꼰꼲꼴꼼꼽꼿꽁꽂꽃꽈꽉꽐꽜꽝꽤꽥꽹꾀꾄꾈꾐꾑꾕꾜꾸꾹꾼꿀꿇꿈꿉꿋꿍꿎꿔꿜꿨꿩꿰꿱꿴꿸뀀뀁뀄뀌뀐뀔뀜뀝뀨끄끅끈끊끌끎끓끔끕끗끙끝끼끽낀낄낌낍낏낑나낙낚난낟날낡낢남납낫났낭낮낯낱낳내낵낸낼냄냅냇냈냉냐냑냔냘냠냥너넉넋넌널넒넓넘넙넛넝넣네넥넨넬넴넵넷넸넹녀녁년녈념녑녔녕녘녜녠노녹논놀놂놈놉놋농높놓놔놘놜놨뇌뇐뇔뇜뇝뇟뇨뇩뇬뇰뇹뇻뇽누눅눈눋눌눔눕눗눙눠눴눼뉘뉜뉠뉨뉩뉴뉵뉼늄늅늉느늑는늘늙늚늠늡늣능늦늪늬늰늴니닉닌닐닒님닙닛닝닢다닥닦단닫달닭닮닯닳담답닷닸당닺닻닿대댁댄댈댐댑댓댔댕댜더덕덖던덛덜덞덟덤덥덧덩덫덮데덱덴델뎀뎁뎃뎄뎅뎌뎐뎔뎠뎡뎨뎬도독돈돋돌돎돐돔돕돗동돛돝돠돤돨돼됐되된될됨됩됫됴두둑둔둘둠둡둣둥둬뒀뒈뒝뒤뒨뒬뒵뒷뒹듀듄듈듐듕드득든듣들듦듬듭듯등듸디딕딘딛딜딤딥딧딨딩딪따딱딴딸땀땁땃땄땅땋때땍땐땔땜땝땟땠땡떠떡떤떨떪떫떰떱떳떴떵떻떼떽뗀뗄뗌뗍뗏뗐뗑뗘뗬또똑똔똘똥똬똴뙈뙤뙨뚜뚝뚠뚤뚫뚬뚱뛔뛰뛴뛸뜀뜁뜅뜨뜩뜬뜯뜰뜸뜹뜻띄띈띌띔띕띠띤띨띰띱띳띵라락란랄람랍랏랐랑랒랖랗래랙랜랠램랩랫랬랭랴략랸럇량러럭런럴럼럽럿렀렁렇레렉렌렐렘렙렛렝려력련렬렴렵렷렸령례롄롑롓로록론롤롬롭롯롱롸롼뢍뢨뢰뢴뢸룀룁룃룅료룐룔룝룟룡루룩룬룰룸룹룻룽뤄뤘뤠뤼뤽륀륄륌륏륑류륙륜률륨륩륫륭르륵른를름릅릇릉릊릍릎리릭린릴림립릿링마막만많맏말맑맒맘맙맛망맞맡맣매맥맨맬맴맵맷맸맹맺먀먁먈먕머먹먼멀멂멈멉멋멍멎멓메멕멘멜멤멥멧멨멩며멱면멸몃몄명몇몌모목몫몬몰몲몸몹못몽뫄뫈뫘뫙뫼묀묄묍묏묑묘묜묠묩묫무묵묶문묻물묽묾뭄뭅뭇뭉뭍뭏뭐뭔뭘뭡뭣뭬뮈뮌뮐뮤뮨뮬뮴뮷므믄믈믐믓미믹민믿밀밂밈밉밋밌밍및밑바박밖밗반받발밝밞밟밤밥밧방밭배백밴밸뱀뱁뱃뱄뱅뱉뱌뱍뱐뱝버벅번벋벌벎범법벗벙벚베벡벤벧벨벰벱벳벴벵벼벽변별볍볏볐병볕볘볜보복볶본볼봄봅봇봉봐봔봤봬뵀뵈뵉뵌뵐뵘뵙뵤뵨부북분붇불붉붊붐붑붓붕붙붚붜붤붰붸뷔뷕뷘뷜뷩뷰뷴뷸븀븃븅브븍븐블븜븝븟비빅빈빌빎빔빕빗빙빚빛빠빡빤빨빪빰빱빳빴빵빻빼빽뺀뺄뺌뺍뺏뺐뺑뺘뺙뺨뻐뻑뻔뻗뻘뻠뻣뻤뻥뻬뼁뼈뼉뼘뼙뼛뼜뼝뽀뽁뽄뽈뽐뽑뽕뾔뾰뿅뿌뿍뿐뿔뿜뿟뿡쀼쁑쁘쁜쁠쁨쁩삐삑삔삘삠삡삣삥사삭삯산삳살삵삶삼삽삿샀상샅새색샌샐샘샙샛샜생샤샥샨샬샴샵샷샹섀섄섈섐섕서석섞선섣설섦섧섬섭섯섰성섶세섹센셀셈셉셋셌셍셔셕션셜셤셥셧셨셩셰셴셸솅소속솎손솔솖솜솝솟송솥솨솩솬솰솽쇄쇈쇌쇔쇗쇘쇠쇤쇨쇰쇱쇳쇼쇽숀숄숌숍숏숑수숙순숟술숨숩숫숭숯숱숲숴쉈쉐쉑쉔쉘쉠쉥쉬쉭쉰쉴쉼쉽쉿슁슈슉슐슘슛슝스슥슨슬슭슴습슷승시식신싣실싫심십싯싱싶싸싹싻싼쌀쌈쌉쌌쌍쌓쌔쌕쌘쌜쌤쌥쌨쌩썅써썩썬썰썲썸썹썼썽쎄쎈쎌쏀쏘쏙쏜쏟쏠쏢쏨쏩쏭쏴쏵쏸쐈쐐쐤쐬쐰쐴쐼쐽쑈쑤쑥쑨쑬쑴쑵쑹쒀쒔쒜쒸쒼쓩쓰쓱쓴쓸쓺쓿씀씁씌씐씔씜씨씩씬씰씸씹씻씽아악안앉않알앍앎앓암압앗았앙앝앞애액앤앨앰앱앳앴앵야약얀얄얇얌얍얏양얕얗얘얜얠얩어억언얹얻얼얽얾엄업없엇었엉엊엌엎에엑엔엘엠엡엣엥여역엮연열엶엷염엽엾엿였영옅옆옇예옌옐옘옙옛옜오옥온올옭옮옰옳옴옵옷옹옻와왁완왈왐왑왓왔왕왜왝왠왬왯왱외왹왼욀욈욉욋욍요욕욘욜욤욥욧용우욱운울욹욺움웁웃웅워웍원월웜웝웠웡웨웩웬웰웸웹웽위윅윈윌윔윕윗윙유육윤율윰윱윳융윷으윽은을읊음읍읏응읒읓읔읕읖읗의읜읠읨읫이익인일읽읾잃임입잇있잉잊잎자작잔잖잗잘잚잠잡잣잤장잦재잭잰잴잼잽잿쟀쟁쟈쟉쟌쟎쟐쟘쟝쟤쟨쟬저적전절젊점접젓정젖제젝젠젤젬젭젯젱져젼졀졈졉졌졍졔조족존졸졺좀좁좃종좆좇좋좌좍좔좝좟좡좨좼좽죄죈죌죔죕죗죙죠죡죤죵주죽준줄줅줆줌줍줏중줘줬줴쥐쥑쥔쥘쥠쥡쥣쥬쥰쥴쥼즈즉즌즐즘즙즛증지직진짇질짊짐집짓징짖짙짚짜짝짠짢짤짧짬짭짯짰짱째짹짼쨀쨈쨉쨋쨌쨍쨔쨘쨩쩌쩍쩐쩔쩜쩝쩟쩠쩡쩨쩽쪄쪘쪼쪽쫀쫄쫌쫍쫏쫑쫓쫘쫙쫠쫬쫴쬈쬐쬔쬘쬠쬡쭁쭈쭉쭌쭐쭘쭙쭝쭤쭸쭹쮜쮸쯔쯤쯧쯩찌찍찐찔찜찝찡찢찧차착찬찮찰참찹찻찼창찾채책챈챌챔챕챗챘챙챠챤챦챨챰챵처척천철첨첩첫첬청체첵첸첼쳄쳅쳇쳉쳐쳔쳤쳬쳰촁초촉촌촐촘촙촛총촤촨촬촹최쵠쵤쵬쵭쵯쵱쵸춈추축춘출춤춥춧충춰췄췌췐취췬췰췸췹췻췽츄츈츌츔츙츠측츤츨츰츱츳층치칙친칟칠칡침칩칫칭카칵칸칼캄캅캇캉캐캑캔캘캠캡캣캤캥캬캭컁커컥컨컫컬컴컵컷컸컹케켁켄켈켐켑켓켕켜켠켤켬켭켯켰켱켸코콕콘콜콤콥콧콩콰콱콴콸쾀쾅쾌쾡쾨쾰쿄쿠쿡쿤쿨쿰쿱쿳쿵쿼퀀퀄퀑퀘퀭퀴퀵퀸퀼큄큅큇큉큐큔큘큠크큭큰클큼큽킁키킥킨킬킴킵킷킹타탁탄탈탉탐탑탓탔탕태택탠탤탬탭탯탰탱탸턍터턱턴털턺텀텁텃텄텅테텍텐텔템텝텟텡텨텬텼톄톈토톡톤톨톰톱톳통톺톼퇀퇘퇴퇸툇툉툐투툭툰툴툼툽툿퉁퉈퉜퉤튀튁튄튈튐튑튕튜튠튤튬튱트특튼튿틀틂틈틉틋틔틘틜틤틥티틱틴틸팀팁팃팅파팍팎판팔팖팜팝팟팠팡팥패팩팬팰팸팹팻팼팽퍄퍅퍼퍽펀펄펌펍펏펐펑페펙펜펠펨펩펫펭펴편펼폄폅폈평폐폘폡폣포폭폰폴폼폽폿퐁퐈퐝푀푄표푠푤푭푯푸푹푼푿풀풂품풉풋풍풔풩퓌퓐퓔퓜퓟퓨퓬퓰퓸퓻퓽프픈플픔픕픗피픽핀필핌핍핏핑하학한할핥함합핫항해핵핸핼햄햅햇했행햐향허헉헌헐헒험헙헛헝헤헥헨헬헴헵헷헹혀혁현혈혐협혓혔형혜혠혤혭호혹혼홀홅홈홉홋홍홑화확환활홧황홰홱홴횃횅회획횐횔횝횟횡효횬횰횹횻후훅훈훌훑훔훗훙훠훤훨훰훵훼훽휀휄휑휘휙휜휠휨휩휫휭휴휵휸휼흄흇흉흐흑흔흖흗흘흙흠흡흣흥흩희흰흴흼흽힁히힉힌힐힘힙힛힝ㄱㄴㄷㄹㅁㅂㅅㅇㅈㅊㅋㅌㅍㅎㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ";
            String digits = "0123456789%:!?.,";  // 숫자와 특수문자

            // 중간 폰트 (48px) - 팁용
            parameter.size = 48;
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + koreanChars + digits;
            loadingFont = generator.generateFont(parameter);
            Logger.info("로딩 화면용 중간 폰트 생성 완료");

            // 큰 폰트 (72px) - 퍼센트용
            parameter.size = 72;
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + koreanChars + digits;
            loadingFontLarge = generator.generateFont(parameter);
            Logger.info("로딩 화면용 큰 폰트 생성 완료");

            generator.dispose();

        } catch (Exception e) {
            Logger.error("로딩 화면 초기화 실패", e);
        }
    }

    @Override
    public void render(float delta) {
        loadingTime += delta;

        // 화면 클리어 (검은색)
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 카메라 업데이트 및 적용
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // 메인 스레드에서 점진적으로 에셋 로드
        assetManager.loadAssetsStep(delta);

        // 로딩 진행률 가져오기
        float progress = assetManager.getProgress();

        // 배경 렌더링 (전체 화면 채우기)
        batch.begin();
        if (loadingBackground != null) {
            // 화면 전체에 배경 이미지 표시 (스케일링)
            batch.draw(loadingBackground, 0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        }
        batch.end();

        // 로딩 바 렌더링
        renderLoadingBar(progress);

        // 로딩 팁 렌더링 (로딩 화면용 폰트가 있으면 처음부터 표시)
        renderTip();

        // 100% 도달 시 추가 대기 시간 카운트
        if (progress >= 1.0f && assetManager.isLoaded() && loadingTime >= Constants.LOADING_MIN_TIME) {
            fullProgressTime += delta;
        }

        // 로딩 완료 확인 (100% 도달 후 1초 대기 후 전환)
        if (!transitionStarted && progress >= 1.0f && assetManager.isLoaded() &&
            loadingTime >= Constants.LOADING_MIN_TIME && fullProgressTime >= WAIT_AFTER_100_PERCENT) {
            transitionStarted = true;
            assetsLoaded = true;
            Logger.info("로딩 완료! 메인 메뉴로 전환 (100% 후 " + fullProgressTime + "초 대기)");

            // 바로 메인 메뉴로 전환 (애니메이션은 MainMenuScreen에서 처리)
            game.setScreen(new MainMenuScreen(game));
        }
    }

    /**
     * 로딩 바를 렌더링합니다. (현대적인 모바일 게임 스타일 - 둥근 모서리)
     *
     * @param progress 로딩 진행률 (0.0 ~ 1.0)
     */
    private void renderLoadingBar(float progress) {
        float barX = (Constants.SCREEN_WIDTH - LOADING_BAR_WIDTH) / 2f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 배경 (진한 회색) - 둥근 사각형
        shapeRenderer.setColor(0.2f, 0.2f, 0.25f, 1f);
        drawRoundedRect(shapeRenderer, barX, LOADING_BAR_Y, LOADING_BAR_WIDTH, LOADING_BAR_HEIGHT, LOADING_BAR_RADIUS);

        // 진행 바 (밝은 파란색) - 둥근 사각형
        if (progress > 0.01f) {
            float currentWidth = Math.max(LOADING_BAR_RADIUS * 2, LOADING_BAR_WIDTH * progress);

            shapeRenderer.setColor(0.2f, 0.6f, 1f, 1f);
            drawRoundedRect(shapeRenderer,
                          barX + LOADING_BAR_PADDING,
                          LOADING_BAR_Y + LOADING_BAR_PADDING,
                          currentWidth - (LOADING_BAR_PADDING * 2),
                          LOADING_BAR_HEIGHT - (LOADING_BAR_PADDING * 2),
                          LOADING_BAR_RADIUS - LOADING_BAR_PADDING);
        }

        shapeRenderer.end();

        // 진행률 텍스트 (로딩 화면용 폰트 사용)
        if (loadingFontLarge != null) {
            batch.begin();
            String progressText = (int)(progress * 100) + "%";

            // 텍스트 중앙 정렬
            com.badlogic.gdx.graphics.g2d.GlyphLayout layout =
                new com.badlogic.gdx.graphics.g2d.GlyphLayout(loadingFontLarge, progressText);
            float textX = (Constants.SCREEN_WIDTH - layout.width) / 2f;
            float textY = LOADING_BAR_Y + LOADING_BAR_HEIGHT + 100;

            // 텍스트 그림자
            loadingFontLarge.setColor(0f, 0f, 0f, 0.7f);
            loadingFontLarge.draw(batch, progressText, textX + 4, textY - 4);

            // 메인 텍스트
            loadingFontLarge.setColor(Color.WHITE);
            loadingFontLarge.draw(batch, progressText, textX, textY);

            batch.end();
        }
    }

    /**
     * 둥근 모서리 사각형을 그립니다.
     *
     * @param shapeRenderer ShapeRenderer 인스턴스
     * @param x X 좌표
     * @param y Y 좌표
     * @param width 너비
     * @param height 높이
     * @param radius 둥근 모서리 반경
     */
    private void drawRoundedRect(ShapeRenderer shapeRenderer, float x, float y, float width, float height, float radius) {
        // 중앙 사각형
        shapeRenderer.rect(x + radius, y, width - 2 * radius, height);
        shapeRenderer.rect(x, y + radius, width, height - 2 * radius);

        // 네 모서리의 원
        shapeRenderer.circle(x + radius, y + radius, radius, 10);
        shapeRenderer.circle(x + width - radius, y + radius, radius, 10);
        shapeRenderer.circle(x + radius, y + height - radius, radius, 10);
        shapeRenderer.circle(x + width - radius, y + height - radius, radius, 10);
    }

    /**
     * 로딩 팁을 렌더링합니다.
     */
    private void renderTip() {
        if (loadingFont != null) {
            batch.begin();

            // 텍스트 중앙 정렬
            com.badlogic.gdx.graphics.g2d.GlyphLayout layout =
                new com.badlogic.gdx.graphics.g2d.GlyphLayout(loadingFont, tipText);
            float textX = (Constants.SCREEN_WIDTH - layout.width) / 2f;

            // 텍스트 그림자
            loadingFont.setColor(0f, 0f, 0f, 0.7f);
            loadingFont.draw(batch, tipText, textX + 2, TIP_Y - 2);

            // 메인 텍스트
            loadingFont.setColor(0.8f, 0.8f, 0.8f, 1f);
            loadingFont.draw(batch, tipText, textX, TIP_Y);

            batch.end();
        }
    }

    @Override
    public void resize(int width, int height) {
        // 뷰포트 업데이트 (화면 크기 변경 시 자동으로 비율 유지)
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
        // 일시정지 처리 (필요 시)
    }

    @Override
    public void resume() {
        // 재개 처리 (필요 시)
    }

    @Override
    public void hide() {
        Logger.info("로딩 화면 숨김");
    }

    @Override
    public void dispose() {
        Logger.info("로딩 화면 리소스 해제");

        if (batch != null) {
            batch.dispose();
        }

        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }

        if (loadingBackground != null) {
            loadingBackground.dispose();
        }

        if (loadingFont != null) {
            loadingFont.dispose();
        }

        if (loadingFontLarge != null) {
            loadingFontLarge.dispose();
        }

        if (transition != null) {
            transition.dispose();
        }
    }
}
