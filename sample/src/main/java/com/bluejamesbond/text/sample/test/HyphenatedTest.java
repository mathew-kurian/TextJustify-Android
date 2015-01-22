/*
 * Copyright 2014 Mathew Kurian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * -------------------------------------------------------------------------
 *
 * PlainTextTest.java
 * @author Mathew Kurian
 *
 * From TextJustify-Android Library v2.0
 * https://github.com/bluejamesbond/TextJustify-Android
 *
 * Please report any issues
 * https://github.com/bluejamesbond/TextJustify-Android/issues
 *
 * Date: 11/1/14 3:21 AM
 */

package com.bluejamesbond.text.sample.test;

import android.os.Bundle;

import com.bluejamesbond.text.DocumentView;
import com.bluejamesbond.text.hyphen.DefaultHyphenator;
import com.bluejamesbond.text.hyphen.DefaultHyphenator.HyphenPattern;
import com.bluejamesbond.text.sample.helper.TestActivity;
import com.bluejamesbond.text.style.TextAlignment;

public class HyphenatedTest extends TestActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DocumentView documentView = addDocumentView(new StringBuilder()
                .append("Vágner Mancini hoje é um sujeito tão angustiado quanto otimista, por mais que pareça uma contradição. O técnico do Botafogo tem a difícil missão de salvar " +
                        "o time do rebaixamento no Campeonato Brasileiro, apesar de todas as dificuldades que o próprio clube impõe a seu trabalho - salários atrasados, afastamento " +
                        "de jogadores importantes, dívidas que ameaçam até as poucas chances que ainda existem de permanência na Série A em 2015. Na manhã desta segunda-feira, Mancini " +
                        "falou ao blog.")
                .toString(), DocumentView.PLAIN_TEXT);

        documentView.getDocumentLayoutParams().setTextAlignment(TextAlignment.JUSTIFIED);
        documentView.getDocumentLayoutParams().setHyphenator(DefaultHyphenator.getInstance(HyphenPattern.PT));
        documentView.getDocumentLayoutParams().setHyphenated(true);
    }
}
