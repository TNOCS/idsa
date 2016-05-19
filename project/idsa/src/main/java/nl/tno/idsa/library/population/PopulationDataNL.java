package nl.tno.idsa.library.population;

import nl.tno.idsa.framework.population.Gender;
import nl.tno.idsa.framework.population.HouseholdTypes;
import nl.tno.idsa.framework.population.PopulationData;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PopulationDataNL extends PopulationData {
    public PopulationDataNL() {
        super(5, 100);

        householdTypes.put(HouseholdTypes.SINGLE, 2784943);//Single
        householdTypes.put(HouseholdTypes.PAIR, 8456335);//Pair
        householdTypes.put(HouseholdTypes.SINGLE_PARENT, 501903);//SingleParent

        //////////////////////////////////////////////////////
        // Single genders
        Map<Gender, Integer> singleGender = new EnumMap<>(Gender.class);
        singleGender.put(Gender.MALE, 1302394);//Male
        singleGender.put(Gender.FEMALE, 1482549);//Female
        householdGenderDistributions.put(HouseholdTypes.SINGLE, singleGender);
        // Pair genders
        Map<Gender, Integer> pairGender = new EnumMap<>(Gender.class);
        pairGender.put(Gender.MALE, 4235893);//Male
        pairGender.put(Gender.FEMALE, 4220442);//Female
        householdGenderDistributions.put(HouseholdTypes.PAIR, pairGender);
        // Single parent genders
        Map<Gender, Integer> singleParentGender = new EnumMap<>(Gender.class);
        singleParentGender.put(Gender.MALE, 81998);//Male
        singleParentGender.put(Gender.FEMALE, 419905);//Female
        householdGenderDistributions.put(HouseholdTypes.SINGLE_PARENT, singleParentGender);

        /////////////////////////////////////////////////////
        // Single ages
        Map<Gender, List<Integer>> singleAge = new EnumMap<>(Gender.class);
        ageDistributions.put(HouseholdTypes.SINGLE, singleAge);
        // Single male ages
        {
            List<Integer> ages = new ArrayList<>(maxAge / ageStepSize);
            ages.add(0);//0-5
            ages.add(0);//5-10
            ages.add(0);//10-15
            ages.add(22800);//15-20
            ages.add(125188);//20-25
            ages.add(156761);//25-30
            ages.add(126373);//30-35
            ages.add(106185);//35-40
            ages.add(114788);//40-45
            ages.add(111810);//45-50
            ages.add(107442);//50-55
            ages.add(95475);//55-60
            ages.add(85613);//60-65
            ages.add(76666);//65-70
            ages.add(55327);//70-75
            ages.add(45883);//75-80
            ages.add(37552);//80-85
            ages.add(23493);//85-90
            ages.add(9379);//90-95
            ages.add(1659);//95-100
            singleAge.put(Gender.MALE, ages);
        }
        // Single female ages
        {
            List<Integer> ages = new ArrayList<>(maxAge / ageStepSize);
            ages.add(0);//0-5
            ages.add(0);//5-10
            ages.add(0);//10-15
            ages.add(30166);//15-20
            ages.add(135460);//20-25
            ages.add(119959);//25-30
            ages.add(76673);//30-35
            ages.add(54715);//35-40
            ages.add(54166);//40-45
            ages.add(60015);//45-50
            ages.add(80486);//50-55
            ages.add(100171);//55-60
            ages.add(114845);//60-65
            ages.add(130741);//65-70
            ages.add(123137);//70-75
            ages.add(131992);//75-80
            ages.add(130202);//80-85
            ages.add(91407);//85-90
            ages.add(39745);//90-95
            ages.add(8669);//95-100
            singleAge.put(Gender.FEMALE, ages);
        }
        // Pair ages
        Map<Gender, List<Integer>> pairAge = new EnumMap<>(Gender.class);
        ageDistributions.put(HouseholdTypes.PAIR, pairAge);
        // Pair male ages
        {
            List<Integer> ages = new ArrayList<>(maxAge / ageStepSize);
            pairAge.put(Gender.MALE, ages);
            ages.add(0);//0-5
            ages.add(0);//5-10
            ages.add(0);//10-15
            ages.add(4484);//15-20
            ages.add(67614);//20-25
            ages.add(226289);//25-30
            ages.add(319960);//30-35
            ages.add(361652);//35-40
            ages.add(457912);//40-45
            ages.add(486998);//45-50
            ages.add(476419);//50-55
            ages.add(436068);//55-60
            ages.add(410681);//60-65
            ages.add(392857);//65-70
            ages.add(263192);//70-75
            ages.add(178654);//75-80
            ages.add(103544);//80-85
            ages.add(40149);//85-90
            ages.add(8643);//90-95
            ages.add(777);//95-100
        }
        // Pair female ages
        {
            List<Integer> ages = new ArrayList<>(maxAge / ageStepSize);
            pairAge.put(Gender.FEMALE, ages);
            ages.add(0);//0-5
            ages.add(0);//5-10
            ages.add(0);//10-15
            ages.add(11101);//15-20
            ages.add(144159);//20-25
            ages.add(303273);//25-30
            ages.add(366285);//30-35
            ages.add(389168);//35-40
            ages.add(469840);//40-45
            ages.add(480297);//45-50
            ages.add(466490);//50-55
            ages.add(419492);//55-60
            ages.add(379120);//60-65
            ages.add(344945);//65-70
            ages.add(215851);//70-75
            ages.add(134733);//75-80
            ages.add(68667);//80-85
            ages.add(22417);//85-90
            ages.add(4147);//90-95
            ages.add(457);//95-100
        }
        // Single parent ages
        Map<Gender, List<Integer>> singleParentAge = new EnumMap<>(Gender.class);
        ageDistributions.put(HouseholdTypes.SINGLE_PARENT, singleParentAge);
        // Single parent male ages
        {
            List<Integer> ages = new ArrayList<>(maxAge / ageStepSize);
            singleParentAge.put(Gender.MALE, ages);
            ages.add(0);//0-5
            ages.add(0);//5-10
            ages.add(0);//10-15
            ages.add(22);//15-20
            ages.add(199);//20-25
            ages.add(597);//25-30
            ages.add(1316);//30-35
            ages.add(3279);//35-40
            ages.add(9518);//40-45
            ages.add(17333);//45-50
            ages.add(19231);//50-55
            ages.add(13306);//55-60
            ages.add(6832);//60-65
            ages.add(3746);//65-70
            ages.add(2190);//70-75
            ages.add(1659);//75-80
            ages.add(1435);//80-85
            ages.add(896);//85-90
            ages.add(357);//90-95
            ages.add(82);//95-100                
        }
        // Single parent female ages
        {
            List<Integer> ages = new ArrayList<>(maxAge / ageStepSize);
            singleParentAge.put(Gender.FEMALE, ages);
            ages.add(0);//0-5
            ages.add(0);//5-10
            ages.add(0);//10-15
            ages.add(1524);//15-20
            ages.add(13689);//20-25
            ages.add(26512);//25-30
            ages.add(36788);//30-35
            ages.add(48593);//35-40
            ages.add(75579);//40-45
            ages.add(81440);//45-50
            ages.add(61252);//50-55
            ages.add(30697);//55-60
            ages.add(13340);//60-65
            ages.add(8098);//65-70
            ages.add(5906);//70-75
            ages.add(5661);//75-80
            ages.add(5135);//80-85
            ages.add(3553);//85-90
            ages.add(1698);//90-95
            ages.add(440);//95-100                
        }

        ///////////////////////////////////////////////////////            
        List<List<Integer>> pairChildren = new ArrayList<>();
        numChildren.put(HouseholdTypes.PAIR, pairChildren);

        ArrayList<Integer> children = new ArrayList<Integer>();

        //4 categories: 0 kids, 1 kid, 2 kids and 3+ kids
        children.add(0);
        children.add(0);
        children.add(0);
        children.add(0);//0-5
        pairChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(0);
        children.add(0);
        children.add(0);
        children.add(0);//5-10
        pairChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(0);
        children.add(0);
        children.add(0);
        children.add(0);//10-15
        pairChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(2424);
        children.add(278);
        children.add(75);
        children.add(43);//15-20
        pairChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(50434);
        children.add(7064);
        children.add(1838);
        children.add(386);//20-25
        pairChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(149682);
        children.add(43242);
        children.add(19812);
        children.add(4437);//25-30
        pairChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(117965);
        children.add(89049);
        children.add(82367);
        children.add(23203);//30-35
        pairChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(68807);
        children.add(78121);
        children.add(148472);
        children.add(59964);//35-40
        pairChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(70954);
        children.add(81206);
        children.add(207293);
        children.add(93399);//40-45
        pairChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(87016);
        children.add(93582);
        children.add(208108);
        children.add(93094);//45-50
        pairChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(137758);
        children.add(126746);
        children.add(152299);
        children.add(56218);//50-55
        pairChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(231138);
        children.add(117564);
        children.add(66966);
        children.add(18864);//55-60
        pairChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(322450);
        children.add(63908);
        children.add(20271);
        children.add(5050);//60-65
        pairChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(356957);
        children.add(29552);
        children.add(5933);
        children.add(1810);//65-70
        pairChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(249078);
        children.add(13305);
        children.add(1955);
        children.add(661);//70-75
        pairChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(173577);
        children.add(7068);
        children.add(778);
        children.add(226);//75-80
        pairChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(101453);
        children.add(3670);
        children.add(343);
        children.add(83);//80-85
        pairChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(39867);
        children.add(1382);
        children.add(134);
        children.add(27);//85-90
        pairChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(8846);
        children.add(296);
        children.add(26);
        children.add(10);//90-95
        pairChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(764);
        children.add(25);
        children.add(3);
        children.add(2);//95-100
        pairChildren.add(children);

        List<List<Integer>> singleParentChildren = new ArrayList<>();
        numChildren.put(HouseholdTypes.SINGLE_PARENT, singleParentChildren);
        //3 categories: 1 kid, 2 kids and 3+ kids
        children = new ArrayList<Integer>();
        children.add(0);
        children.add(0);
        children.add(0);//0-5
        singleParentChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(0);
        children.add(0);
        children.add(0);//5-10
        singleParentChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(0);
        children.add(0);
        children.add(0);//10-15
        singleParentChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(1050);
        children.add(58);
        children.add(3);//15-20
        singleParentChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(8019);
        children.add(1860);
        children.add(282);//20-25
        singleParentChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(16646);
        children.add(7434);
        children.add(2170);//25-30
        singleParentChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(20857);
        children.add(13711);
        children.add(5207);//30-35
        singleParentChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(26106);
        children.add(20452);
        children.add(8760);//35-40
        singleParentChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(43599);
        children.add(34086);
        children.add(11928);//40-45
        singleParentChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(55653);
        children.add(36795);
        children.add(10918);//45-50
        singleParentChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(55187);
        children.add(25437);
        children.add(5937);//50-55
        singleParentChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(36728);
        children.add(10722);
        children.add(1722);//55-60
        singleParentChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(18820);
        children.add(3237);
        children.add(475);//60-65
        singleParentChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(11486);
        children.add(1199);
        children.add(185);//65-70
        singleParentChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(7901);
        children.add(726);
        children.add(93);//70-75
        singleParentChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(7639);
        children.add(751);
        children.add(79);//75-80
        singleParentChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(7558);
        children.add(672);
        children.add(80);//80-85
        singleParentChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(5315);
        children.add(427);
        children.add(52);//85-90
        singleParentChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(2443);
        children.add(189);
        children.add(22);//90-95
        singleParentChildren.add(children);
        children = new ArrayList<Integer>();
        children.add(622);
        children.add(49);
        children.add(2);//95-100
        singleParentChildren.add(children);

        ////////////////////////////////////////////////////////////////////
        childrenGenderDistributions.put(Gender.MALE, 2422333);//Male
        childrenGenderDistributions.put(Gender.FEMALE, 2086049);//Female

        ////////////////////////////////////////////////////////////////////
        {
            List<Integer> ages = new ArrayList<>(maxAge / ageStepSize);
            childrenAgeDistributions.put(Gender.MALE, ages);
            ages.add(453985);//0-5
            ages.add(473077);//5-10
            ages.add(513759);//10-15
            ages.add(467709);//15-20
            ages.add(307197);//20-25
            ages.add(106414);//25-30
            ages.add(33599);//30-35
            ages.add(19014);//35-40
            ages.add(16758);//40-45
            ages.add(13806);//45-50
            ages.add(9185);//50-55
            ages.add(4558);//55-60
            ages.add(2392);//60-65
            ages.add(785);//65-70
            ages.add(79);//70-75
            ages.add(16);//75-80
            ages.add(0);//80-85
            ages.add(0);//85-90
            ages.add(0);//90-95
            ages.add(0);//95-100
        }
        {
            List<Integer> ages = new ArrayList<>(maxAge / ageStepSize);
            childrenAgeDistributions.put(Gender.FEMALE, ages);
            ages.add(433748);//0-5
            ages.add(453695);//5-10
            ages.add(491090);//10-15
            ages.add(429875);//15-20
            ages.add(199363);//20-25
            ages.add(45568);//25-30
            ages.add(12242);//30-35
            ages.add(6070);//35-40
            ages.add(4839);//40-45
            ages.add(3820);//45-50
            ages.add(2869);//50-55
            ages.add(1664);//55-60
            ages.add(793);//60-65
            ages.add(308);//65-70
            ages.add(90);//70-75
            ages.add(15);//75-80
            ages.add(0);//80-85
            ages.add(0);//85-90
            ages.add(0);//90-95
            ages.add(0);//95-100
        }
    }
}