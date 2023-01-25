require: slotfilling/slotFilling.sc
  module = sys.zb-common
  
require: text/text.sc
    module = zenbot-common
    
require: where/where.sc
    module = zenbot-common

require: common.js
    module = zenbot-common

require: hangmanGameData.csv
    name = HangmanGameData
    var = $HangmanGameData

require: functions.js

patterns:
    $Word = $entity<HangmanGameData> || converter = function ($parseTree) {
        var id = $parseTree.HangmanGameData[0].value;
        return $HangmanGameData[id].value;
        }

theme: /

    state: Start
        q!: $regex</start>
        intent!: /LetsPlay
        script: 
            $session = {}
        a: Привет! Предлагаю сыграть в игру "Виселица". Я загадаю слово, а ты будешь отгадывать. Ты можешь угадывать по буквам или попробовать назвать слово целиком. Можно допустить 6 ошибок. Начнем?
        go!: /Start/Ready
        
        state: Ready
            
            state: Agree
                intent: /Agree
                go!: /Game
            
            state: Disagree
                intent: /Disagree
                a: Если передумаешь - скажи "давай поиграем"
        
    state: Game
        script: 
            $session.keys = Object.keys($HangmanGameData)
            $session.word = $HangmanGameData[getRandomWordKey($session.keys)].value.word
            $session.underscored = setUnderscores($session.word)
            $session.mistakes = 0
            $reactions.answer("{{$session.word.length}} букв\n\n {{$session.underscored}}")
            $reactions.answer("Назови букву или все слово целиком")
        go: /Guessing

    state: Guessing
        
        state: WordPattern
            q: $nonEmptyGarbage
            script:
                var userAnswer = $parseTree.text.toLowerCase();
                if(userAnswer.length === 1){
                    if(searchLetter($session.word,userAnswer)){
                        $reactions.answer("Есть такая буква")
                        $session.underscored = changeLetters($session.word,userAnswer,$session.underscored)
                        $reactions.answer("{{$session.underscored}}")
                    }
                    else{
                        $reactions.answer("Нет такой буквы")
                        $session.mistakes+=1
                    }
                }
                else {
                    if($session.word === userAnswer){
                        $reactions.answer("Поздравляю, ты угадал!")
                    }
                    else {
                        $session.mistakes+=1
                        $reactions.answer("Я загадал другое слово")
                    }
                }
                if($session.mistakes === 4){
                    $reactions.answer("Вы можете совершить еще 2 ошибки")
                }
                if($session.mistakes === 5){
                    $reactions.answer("Вы можете совершить еще 1 ошибку")
                }
                if($session.mistakes === 6){
                    $reactions.answer("Увы, ты проиграл. Я загадал слово *{{$session.word}}*")
                    $reactions.transition("/PlayAgain")
                }
                

    state: PlayAgain
        a:  Сыграем еще?
        
        state: Yes
            intent: /Agree
            go!: /Game
        
        state: No
            intent: /Disagree
            go!: /EndGame
            
    state: EndGame
        a: Жаль! Если передумаешь - скажи "давай поиграем"
                
    state: NoMatch || noContext = true
        event!: noMatch
        random:
            a: Я не понял.
            a: Что вы имеете в виду?
            a: Ничего не пойму
         
        
    

