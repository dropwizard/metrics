package com.codahale.metrics;

import static org.assertj.core.api.BDDAssertions.then;

import org.junit.Test;

/**
 * @author bstorozhuk
 */
public class ChunkedAssociativeLongArrayTest {

    @Test
    public void testClear() {
        ChunkedAssociativeLongArray array = new ChunkedAssociativeLongArray(3);
        array.put(-3, 3);
        array.put(-2, 1);
        array.put(0, 5);
        array.put(3, 0);
        array.put(9, 8);
        array.put(15, 0);
        array.put(19, 5);
        array.put(21, 5);
        array.put(34, -9);
        array.put(109, 5);

        then(array.out())
            .isEqualTo("[(-3: 3) (-2: 1) (0: 5) ]->[(3: 0) (9: 8) (15: 0) ]->[(19: 5) (21: 5) (34: -9) ]->[(109: 5) ]");
        then(array.values())
            .isEqualTo(new long[]{3, 1, 5, 0, 8, 0, 5, 5, -9, 5});
        then(array.size())
            .isEqualTo(10);

        array.clear(-2, 20);
        then(array.out())
            .isEqualTo("[(-3: 3) ]->[(21: 5) (34: -9) ]->[(109: 5) ]");
        then(array.values())
            .isEqualTo(new long[]{3, 5, -9, 5});
        then(array.size())
            .isEqualTo(4);
    }


    @Test
    public void testTrim() {
        ChunkedAssociativeLongArray array = new ChunkedAssociativeLongArray(3);
        array.put(-3, 3);
        array.put(-2, 1);
        array.put(0, 5);
        array.put(3, 0);
        array.put(9, 8);
        array.put(15, 0);
        array.put(19, 5);
        array.put(21, 5);
        array.put(34, -9);
        array.put(109, 5);

        then(array.out())
            .isEqualTo("[(-3: 3) (-2: 1) (0: 5) ]->[(3: 0) (9: 8) (15: 0) ]->[(19: 5) (21: 5) (34: -9) ]->[(109: 5) ]");
        then(array.values())
            .isEqualTo(new long[]{3, 1, 5, 0, 8, 0, 5, 5, -9, 5});
        then(array.size())
            .isEqualTo(10);

        array.trim(-2, 20);

        then(array.out())
            .isEqualTo("[(-2: 1) (0: 5) ]->[(3: 0) (9: 8) (15: 0) ]->[(19: 5) ]");
        then(array.values())
            .isEqualTo(new long[]{1, 5, 0, 8, 0, 5});
        then(array.size())
            .isEqualTo(6);

    }

    @Test
    public void megaTest() {
        ChunkedAssociativeLongArray array = new ChunkedAssociativeLongArray(20);

        array.put(9222687636321556736L, 9222450436869623594L);
        array.put(9222687636321556737L, 9222450436869623595L);
        array.put(9222687636321556738L, 9222450436869623596L);
        array.put(9222687636321556739L, 9222450436869623597L);
        array.put(9222687636321556740L, 9222450436869623598L);

        array.put(9222687636321556741L, 9222450436869623599L);
        array.put(9222687636321556742L, 9222450436869623600L);
        array.put(9222687636321556743L, 9222450436869623601L);
        array.put(9222687636321556744L, 9222450436869623602L);
        array.put(9222687636321556745L, 9222450436869623603L);

        array.put(9222687636321556746L, 9222450436869623604L);
        array.put(9222687636321556747L, 9222450436869623605L);
        array.put(9222687636321556748L, 9222450436869623606L);
        array.put(9222687636321556749L, 9222450436869623607L);
        array.put(9222687636321556750L, 9222450436869623608L);

        array.put(9222687636321556751L, 9222450436869623609L);
        array.put(9222687636321556752L, 9222450436869623610L);
        array.put(9222687636321556753L, 9222450436869623611L);
        array.put(9222687636321556754L, 9222450436869623612L);
        array.put(9222687636321556755L, 9222450436869623613L);

        array.put(9222687636321556756L, 9222450436869623614L);
        array.put(9222687636321556757L, 9222450436869623615L);
        array.put(9222687636321556758L, 9222450436869623616L);
        array.put(9222687636321556759L, 9222450436869623617L);
        array.put(9222687636321556760L, 9222450436869623618L);

        array.put(9222687636321556761L, 9222450436869623619L);
        array.put(9222687636321556762L, 9222450436869623620L);
        array.put(9222687636321556763L, 9222450436869623621L);
        array.put(9222687636321556764L, 9222450436869623622L);
        array.put(9222687636321556765L, 9222450436869623623L);

        array.put(9222687636321556766L, 9222450436869623624L);
        array.put(9222687636321556767L, 9222450436869623625L);
        array.put(9222687636321556768L, 9222450436869623626L);
        array.put(9222687636321556769L, 9222450436869623627L);
        array.put(9222687636321556770L, 9222450436869623628L);

        array.put(9222687636321556771L, 9222450436869623629L);
        array.put(9222687636321556772L, 9222450436869623630L);
        array.put(9222687636321556773L, 9222450436869623631L);
        array.put(9222687636321556774L, 9222450436869623632L);
        array.put(9222687636321556775L, 9222450436869623633L);

        array.put(9222687636321556776L, 9222450436869623634L);
        array.put(9222687636321556777L, 9222450436869623635L);
        array.put(9222687636321556778L, 9222450436869623636L);
        array.put(9222687636321556779L, 9222450436869623637L);
        array.put(9222687636321556780L, 9222450436869623638L);

        array.put(9222687636321556781L, 9222450436869623639L);
        array.put(9222687636321556782L, 9222450436869623640L);
        array.put(9222687636321556783L, 9222450436869623641L);
        array.put(9222687636321556784L, 9222450436869623642L);
        array.put(9222687636321556785L, 9222450436869623643L);

        array.put(9222687636321556786L, 9222450436869623644L);
        array.put(9222687636321556787L, 9222450436869623645L);
        array.put(9222687636321556788L, 9222450436869623646L);
        array.put(9222687636321556789L, 9222450436869623647L);
        array.put(9222687636321556790L, 9222450436869623648L);

        array.put(9222687636321556791L, 9222450436869623649L);
        array.put(9222687636321556792L, 9222450436869623650L);
        array.put(9222687636321556793L, 9222450436869623651L);
        array.put(9222687636321556794L, 9222450436869623652L);
        array.put(9222687636321556795L, 9222450436869623653L);

        array.put(9222687636321556796L, 9222450436869623654L);
        array.put(9222687636321556797L, 9222450436869623655L);
        array.put(9222687636321556798L, 9222450436869623656L);
        array.put(9222687636321556799L, 9222450436869623657L);
        array.put(9222687636321556800L, 9222450436869623658L);

        array.put(9222687636321556801L, 9222450436869623659L);
        array.put(9222687636321556802L, 9222450436869623660L);
        array.put(9222687636321556803L, 9222450436869623661L);
        array.put(9222687636321556804L, 9222450436869623662L);
        array.put(9222687636321556805L, 9222450436869623663L);

        array.put(9222687636321556806L, 9222450436869623664L);
        array.put(9222687636321556807L, 9222450436869623665L);
        array.put(9222687636321556808L, 9222450436869623666L);
        array.put(9222687636321556809L, 9222450436869623667L);
        array.put(9222687636321556810L, 9222450436869623668L);

        array.put(9222687636321556811L, 9222450436869623669L);
        array.put(9222687636321556812L, 9222450436869623670L);
        array.put(9222687636321556813L, 9222450436869623671L);
        array.put(9222687636321556814L, 9222450436869623672L);
        array.put(9222687636321556815L, 9222450436869623673L);

        array.put(9222687636321556816L, 9222450436869623674L);
        array.put(9222687636321556817L, 9222450436869623675L);
        array.put(9222687636321556818L, 9222450436869623676L);
        array.put(9222687636321556819L, 9222450436869623677L);
        array.put(9222687636321556820L, 9222450436869623678L);

        array.put(9222687636321556821L, 9222450436869623679L);
        array.put(9222687636321556822L, 9222450436869623680L);
        array.put(9222687636321556823L, 9222450436869623681L);
        array.put(9222687636321556824L, 9222450436869623682L);
        array.put(9222687636321556825L, 9222450436869623683L);

        array.put(9222687636321556826L, 9222450436869623684L);
        array.put(9222687636321556827L, 9222450436869623685L);
        array.put(9222687636321556828L, 9222450436869623686L);
        array.put(9222687636321556829L, 9222450436869623687L);
        array.put(9222687636321556830L, 9222450436869623688L);

        array.put(9222687636321556831L, 9222450436869623689L);
        array.put(9222687636321556832L, 9222450436869623690L);
        array.put(9222687636321556833L, 9222450436869623691L);
        array.put(9222687636321556834L, 9222450436869623692L);
        array.put(9222687636321556835L, 9222450436869623693L);

        array.put(9222687636321556836L, 9222450436869623694L);
        array.put(9222687636321556837L, 9222450436869623695L);
        array.put(9222687636321556838L, 9222450436869623696L);
        array.put(9222687636321556839L, 9222450436869623697L);
        array.put(9222687636321556840L, 9222450436869623698L);

        array.put(9222687636321556841L, 9222450436869623699L);
        array.put(9222687636321556842L, 9222450436869623700L);
        array.put(9222687636321556843L, 9222450436869623701L);
        array.put(9222687636321556844L, 9222450436869623702L);
        array.put(9222687636321556845L, 9222450436869623703L);

        array.put(9222687636321556847L, 9222450436869623704L);

        System.out.println(array.out());

        array.clear(-9223134837387994768L, 9222687636321556080L);
        System.out.println(" ");
        System.out.println(array.out());

//        tree
//        9222687636321556080
//            - 9223134837387994768
    }
}