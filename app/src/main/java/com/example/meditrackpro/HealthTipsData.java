package com.example.meditrackpro;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Fixed, hand-written home remedies and first aid tips shown in {@link RemediesActivity}.
 * Nothing here is generated or personalised; it is the same list for every user.
 */
final class HealthTipsData {

    static final class Tip {
        final String title;
        final String forWhat;
        final List<String> steps;
        final String caution;

        Tip(String title, String forWhat, String[] steps, String caution) {
            this.title = title;
            this.forWhat = forWhat;
            this.steps = Collections.unmodifiableList(Arrays.asList(steps));
            this.caution = caution;
        }
    }

    static final String DISCLAIMER =
            "These are general, commonly used tips for minor problems. They do not replace a doctor's advice "
                    + "or your prescribed medicines. In an emergency, call your local emergency number (112 in India).";

    static final List<Tip> HOME_REMEDIES = Collections.unmodifiableList(Arrays.asList(
            new Tip("Kadha for sore throat", "Sore throat, cough, common cold",
                    new String[]{
                            "Boil 2 cups of water with a few tulsi leaves, a small piece of crushed ginger, "
                                    + "4-5 black peppercorns and a pinch of turmeric.",
                            "Simmer for 5-7 minutes until it reduces to about one cup.",
                            "Strain, add a little honey or jaggery once it is warm, and sip slowly.",
                            "Have it once or twice a day."},
                    "Do not give honey to children under 1 year. Skip pepper if it irritates your stomach."),
            new Tip("Warm towel for headache", "Tension headache",
                    new String[]{
                            "Soak a clean towel in warm (not hot) water and wring it out.",
                            "Lie down in a quiet, dim room and place it on your forehead or the back of your neck "
                                    + "for 10-15 minutes.",
                            "Drink a glass of water and rest your eyes away from screens."},
                    "See a doctor for a sudden severe headache, headache after a head injury, or one with "
                            + "fever, stiff neck, vomiting or vision changes."),
            new Tip("Salt water gargle", "Sore throat, mouth ulcers",
                    new String[]{
                            "Mix half a teaspoon of salt in a glass of warm water.",
                            "Gargle for 30 seconds, then spit it out. Do not swallow.",
                            "Repeat 3-4 times a day."},
                    "Not suitable for young children who cannot gargle."),
            new Tip("Ginger and honey for cough", "Dry or irritating cough",
                    new String[]{
                            "Mix one teaspoon of fresh ginger juice with one teaspoon of honey.",
                            "Take it slowly, up to twice a day.",
                            "Drink warm fluids through the day."},
                    "Not for children under 1 year. See a doctor if the cough lasts more than 2 weeks."),
            new Tip("Steam inhalation", "Blocked nose, congestion",
                    new String[]{
                            "Pour hot water into a wide bowl and sit with your face about 30 cm above it.",
                            "Cover your head with a towel and breathe in the steam for 5 minutes.",
                            "Stop if you feel dizzy or the steam feels too hot."},
                    "Take care with hot water, especially around children. Do not use on a child alone."),
            new Tip("Oral rehydration drink", "Loose motions, mild dehydration",
                    new String[]{
                            "Mix 6 level teaspoons of sugar and half a teaspoon of salt in 1 litre of clean "
                                    + "drinking water, or use an ORS packet as directed.",
                            "Take small sips often, especially after each loose motion.",
                            "Continue normal food such as rice, banana and curd as tolerated."},
                    "See a doctor if there is blood in the stool, high fever, or signs of dehydration such as "
                            + "very dry mouth, no urine for 8 hours or dizziness."),
            new Tip("Jeera or ajwain water for gas", "Indigestion, bloating, gas",
                    new String[]{
                            "Boil one teaspoon of cumin (jeera) or carom seeds (ajwain) in a glass of water "
                                    + "for 5 minutes.",
                            "Strain and sip warm after a meal.",
                            "Walk slowly for 10 minutes and avoid lying down right after eating."},
                    "See a doctor for severe or repeated stomach pain, or pain with vomiting or fever."),
            new Tip("Ginger tea for nausea", "Mild nausea, motion sickness",
                    new String[]{
                            "Boil a few slices of fresh ginger in a cup of water for 5 minutes.",
                            "Sip it warm. A few drops of lemon can be added.",
                            "Sit upright in fresh air and take slow breaths."},
                    "If you take blood-thinning medicine, ask your doctor before using ginger regularly."),
            new Tip("Cold compress for sprain or swelling", "Minor sprain, bumps, swelling",
                    new String[]{
                            "Wrap ice or a cold pack in a thin cloth. Never put ice directly on the skin.",
                            "Apply for 15-20 minutes, then remove for at least an hour.",
                            "Rest and raise the injured part above heart level."},
                    "See a doctor if the part cannot bear weight, looks deformed, or the pain is severe."),
            new Tip("Turmeric milk", "Body ache, mild cold",
                    new String[]{
                            "Stir half a teaspoon of turmeric into a glass of warm milk.",
                            "Drink it before bed.",
                            "Rest and keep warm."},
                    "Avoid if you are allergic to milk. Check with your doctor if you have gallstones."),
            new Tip("Cool water and aloe for mild sunburn", "Mild sunburn, red irritated skin",
                    new String[]{
                            "Move out of the sun and cool the skin with a cool (not icy) damp cloth.",
                            "Apply pure aloe vera gel to the area.",
                            "Drink extra water and keep the area covered outdoors."},
                    "See a doctor if there are large blisters, fever or the burn covers a large area."),
            new Tip("Warm water and rest for sleeplessness", "Trouble falling asleep",
                    new String[]{
                            "Keep a fixed bedtime and avoid tea, coffee and screens for an hour before bed.",
                            "Take a warm shower or have a glass of warm milk.",
                            "Keep the room dark, quiet and cool."},
                    "See a doctor if sleep problems continue for more than 2 weeks.")
    ));

    static final List<Tip> FIRST_AID = Collections.unmodifiableList(Arrays.asList(
            new Tip("Minor cuts and scrapes", "Small bleeding wounds",
                    new String[]{
                            "Wash your hands, then press a clean cloth on the cut to stop the bleeding.",
                            "Rinse the wound under clean running water and remove visible dirt.",
                            "Cover with a clean bandage and change it daily."},
                    "Get medical help for deep cuts, bleeding that does not stop after 10 minutes, or animal "
                            + "bites. Check that tetanus shots are up to date."),
            new Tip("Burns", "Minor burns and scalds",
                    new String[]{
                            "Hold the burn under cool running water for 15-20 minutes.",
                            "Remove rings or tight items near the burn before it swells.",
                            "Cover loosely with a clean, non-fluffy cloth or cling film.",
                            "Do not burst blisters. Do not apply butter, toothpaste or ice."},
                    "Seek urgent help for burns larger than the palm, on the face, hands or genitals, or "
                            + "deep burns."),
            new Tip("Nosebleed", "Bleeding from the nose",
                    new String[]{
                            "Sit up and lean slightly forward. Do not tilt the head back.",
                            "Pinch the soft part of the nose for 10 minutes without releasing.",
                            "Breathe through the mouth and apply a cold cloth on the nose."},
                    "Get help if bleeding lasts more than 20 minutes or follows a head injury."),
            new Tip("Choking (adult)", "Cannot cough, speak or breathe",
                    new String[]{
                            "Ask them to cough. If they cannot, call for help.",
                            "Give up to 5 firm back blows between the shoulder blades.",
                            "Then give up to 5 abdominal thrusts: stand behind, fist above the navel, pull "
                                    + "sharply inwards and upwards.",
                            "Alternate until the object comes out or help arrives."},
                    "Call emergency services if they become unresponsive and start CPR."),
            new Tip("Fainting", "Person feels dizzy or passes out",
                    new String[]{
                            "Lay the person flat and raise their legs about 30 cm.",
                            "Loosen tight clothing and give fresh air.",
                            "Check that they are breathing. Once awake, let them sit up slowly and sip water."},
                    "Call for help if they do not wake within a minute, are injured, or are not breathing."),
            new Tip("Heat exhaustion", "Heavy sweating, weakness, headache in hot weather",
                    new String[]{
                            "Move to a cool, shaded place and lie down.",
                            "Loosen clothing and cool the skin with a wet cloth.",
                            "Sip cool water or ORS slowly."},
                    "Call emergency services for confusion, fits or a very high body temperature: this may be "
                            + "heat stroke."),
            new Tip("Sprain or strain", "Twisted joint or pulled muscle",
                    new String[]{
                            "Follow RICE: Rest, Ice (in a cloth, 15-20 minutes), Compression with a bandage, "
                                    + "Elevation above heart level.",
                            "Avoid putting weight on the area for a day or two."},
                    "Seek care if it looks deformed, you hear a crack, or pain is severe."),
            new Tip("Suspected fracture", "Severe pain, swelling, deformity after a fall",
                    new String[]{
                            "Keep the person still and do not try to straighten the limb.",
                            "Support the limb in the position found using padding or a folded cloth.",
                            "Apply a cold pack wrapped in cloth and get medical help."},
                    "Do not move someone with a suspected neck or back injury. Call emergency services."),
            new Tip("Insect sting or bite", "Bee, wasp or insect sting",
                    new String[]{
                            "Remove a visible sting by scraping it off with a card edge.",
                            "Wash the area and apply a cold cloth to reduce swelling.",
                            "Avoid scratching."},
                    "Call emergency services at once for swelling of the face or throat, trouble breathing "
                            + "or widespread rash."),
            new Tip("Poisoning or overdose", "Swallowed medicine, chemical or unknown substance",
                    new String[]{
                            "Call a poison helpline or emergency services immediately.",
                            "Keep the container or tablets to show the doctor.",
                            "Do not make the person vomit unless a medical professional tells you to."},
                    "Always keep medicines out of reach of children."),
            new Tip("Heart attack warning signs", "Chest pain or pressure, pain spreading to arm or jaw",
                    new String[]{
                            "Call emergency services immediately.",
                            "Make the person sit down and stay calm.",
                            "If they are not allergic and a doctor has not told them otherwise, they may chew "
                                    + "one regular aspirin while waiting."},
                    "Do not let the person walk or drive themselves to hospital.")
    ));

    private HealthTipsData() {
    }
}
