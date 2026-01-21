package com.rafarg.ecogardengame.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class LibraryEntry(
    val id: String,
    val title: String,
    val content: String,
    val cost: ItemCost,
    isUnlockedInitial: Boolean = false
) {
    var isUnlocked by mutableStateOf(isUnlockedInitial)
}

data class LibraryCategory(
    val id: String,
    val name: String,
    val icon: String,
    val entries: List<LibraryEntry>
)

object LibraryRepository {

    private fun createEntry(cat: String, index: Int, title: String, content: String, price: Int): LibraryEntry {
        return LibraryEntry(
            id = "${cat}_$index",
            title = title,
            content = content,
            cost = ItemCost(money = price)
        )
    }

    val tomatoEntries = listOf(
        createEntry("tomato", 1, "The Andean Origin", "The tomato originates from the South American Andes, specifically from the areas now known as Peru, Ecuador, and Chile.", 100),
        createEntry("tomato", 2, "Fruit or Vegetable?", "Botanically, a tomato is a fruit because it develops from the ovary of a flower and contains seeds.", 200),
        createEntry("tomato", 3, "Golden Apples", "The first tomatoes brought to Europe were yellow, which led the Italians to call them 'pomi d'oro' (golden apples).", 300),
        createEntry("tomato", 4, "Lycopene Power", "Tomatoes are the primary dietary source of lycopene, an antioxidant linked to many health benefits, including reduced risk of heart disease.", 400),
        createEntry("tomato", 5, "Genetic Diversity", "There are more than 10,000 varieties of tomatoes grown worldwide, ranging from tiny cherry tomatoes to giant beefsteaks.", 500),
        createEntry("tomato", 6, "Space Tomatoes", "Tomatoes have been successfully grown in the International Space Station to study plant growth in microgravity.", 600),
        createEntry("tomato", 7, "Water Content", "A fresh tomato is about 95% water. The remaining 5% consists mainly of carbohydrates and fiber.", 700),
        createEntry("tomato", 8, "Cooking Benefits", "Unlike many vegetables, cooking tomatoes actually increases the amount of lycopene your body can absorb.", 800),
        createEntry("tomato", 9, "The heaviest Tomato", "The Guinness World Record for the heaviest tomato weighed 3.51 kg (7.7 lb) and was grown in the USA.", 900),
        createEntry("tomato", 10, "La Tomatina", "Every year, the town of Buñol in Spain hosts a festival where 150,000 tomatoes are used as projectiles in a giant food fight.", 1000)
    )

    val broccoliEntries = listOf(
        createEntry("broccoli", 1, "Cabbage Cousin", "Broccoli belongs to the Brassicaceae family, making it a close relative of cabbage, kale, and cauliflower.", 150),
        createEntry("broccoli", 2, "Italian Name", "The name 'broccoli' comes from the Italian word 'broccolo', which means the flowering crest of a cabbage.", 300),
        createEntry("broccoli", 3, "Vitamin C Giant", "A cup of broccoli contains as much vitamin C as an orange, helping boost the immune system.", 450),
        createEntry("broccoli", 4, "Natural Pest Control", "Broccoli produces natural sulfur compounds that act as a defense mechanism against insects and diseases.", 600),
        createEntry("broccoli", 5, "Roman Staple", "Broccoli was a popular food in ancient Rome, where it was often served with various spices and oils.", 750),
        createEntry("broccoli", 6, "Sulforaphane", "It contains sulforaphane, a compound that may protect against certain types of cancer and improve heart health.", 900),
        createEntry("broccoli", 7, "Steam for Health", "Steaming broccoli is the best way to cook it if you want to preserve its cholesterol-lowering properties.", 1050),
        createEntry("broccoli", 8, "Flowering Plant", "The head of broccoli is actually composed of many tiny flower buds that haven't bloomed yet.", 1200),
        createEntry("broccoli", 9, "California Production", "Over 90% of the broccoli grown in the United States comes from California due to its favorable climate.", 1350),
        createEntry("broccoli", 10, "Frozen vs Fresh", "Frozen broccoli can sometimes have more nutrients than fresh broccoli because it is frozen right after being harvested.", 1500)
    )

    val bellPepperEntries = listOf(
        createEntry("bell_pepper", 1, "Heat Levels", "Bell peppers are the only member of the Capsicum family that does not produce capsaicin, making them sweet.", 200),
        createEntry("bell_pepper", 2, "Color Stages", "Most green peppers are just unripe red peppers. As they ripen, they turn from green to yellow, orange, and then red.", 400),
        createEntry("bell_pepper", 3, "Vitamin A Source", "Red bell peppers have 11 times more beta-carotene and 1.5 times more vitamin C than green ones.", 600),
        createEntry("bell_pepper", 4, "South American Roots", "They were first cultivated in Central and South America more than 9,000 years ago.", 800),
        createEntry("bell_pepper", 5, "Hungarian Paprika", "Paprika is made by grinding dried bell peppers and other chili varieties, becoming a staple in Hungarian cuisine.", 1000),
        createEntry("bell_pepper", 6, "Flower to Fruit", "Bell pepper plants usually produce white flowers that eventually develop into the peppers we eat.", 1200),
        createEntry("bell_pepper", 7, "Shelf Life", "Because they contain more sugar, red peppers have a shorter shelf life than green peppers.", 1400),
        createEntry("bell_pepper", 8, "Culinary Versatility", "They can be eaten raw, roasted, stuffed, or fried, and are a key ingredient in 'mirepoix' or 'sofrito'.", 1600),
        createEntry("bell_pepper", 9, "The Purple Variety", "Purple bell peppers exist! They are sweet like green ones but have a beautiful violet skin.", 1800),
        createEntry("bell_pepper", 10, "Weight and Density", "A high-quality pepper should feel heavy for its size, indicating thick, hydrated walls.", 2000)
    )

    val garlicEntries = listOf(
        createEntry("garlic", 1, "The Stinking Rose", "Garlic has been nicknamed 'The Stinking Rose' because of its strong aroma and beneficial properties.", 250),
        createEntry("garlic", 2, "Ancient Medicine", "Ancient Egyptians used garlic to treat everything from heart disease to parasites and even poor circulation.", 500),
        createEntry("garlic", 3, "Allicin Activation", "The compound allicin is only formed when garlic is crushed or chopped, which starts a chemical reaction.", 750),
        createEntry("garlic", 4, "World War I Antiseptic", "Garlic was used during World War I as an antiseptic to prevent gangrene in soldiers' wounds.", 1000),
        createEntry("garlic", 5, "Vampire Lore", "The folklore that garlic repels vampires likely comes from its use as a traditional medicine against blood-related diseases.", 1250),
        createEntry("garlic", 6, "Garlic Breath", "Eating parsley or drinking milk can help neutralize the sulfur compounds that cause garlic breath.", 1500),
        createEntry("garlic", 7, "Bulb Structure", "A single garlic bulb contains about 10 to 20 individual cloves, each protected by a papery skin.", 1750),
        createEntry("garlic", 8, "Gilroy, California", "Gilroy is known as the 'Garlic Capital of the World' and hosts an annual garlic festival featuring garlic ice cream.", 2000),
        createEntry("garlic", 9, "Anti-Inflammatory", "Studies show that garlic can help reduce inflammation and may lower blood pressure in some people.", 2250),
        createEntry("garlic", 10, "Longevity Secret", "Garlic has been a staple in the diets of some of the world's longest-living populations, like in the Mediterranean.", 2500)
    )

    val purpleOnionEntries = listOf(
        createEntry("purple_onion", 1, "Anthocyanins", "The purple color comes from anthocyanins, powerful antioxidants also found in blueberries and red grapes.", 300),
        createEntry("purple_onion", 2, "Mild and Sweet", "Compared to white onions, purple onions tend to be milder and sweeter, making them perfect for raw salads.", 600),
        createEntry("purple_onion", 3, "Tear Jerker", "Onions release a gas called syn-propanethial-S-oxide when cut, which reacts with your eyes to form sulfuric acid.", 900),
        createEntry("purple_onion", 4, "Natural Dye", "The skins of purple onions have been used for centuries as a natural dye for fabrics and Easter eggs.", 1200),
        createEntry("purple_onion", 5, "Layered History", "Onions were worshipped in ancient Egypt; their spherical shape and concentric rings represented eternal life.", 1500),
        createEntry("purple_onion", 6, "Quercetin", "Purple onions are one of the richest sources of quercetin, a flavonoid that helps fight inflammation.", 1800),
        createEntry("purple_onion", 7, "Heart Health", "Regular consumption of red onions may help lower cholesterol and reduce the risk of heart disease.", 2100),
        createEntry("purple_onion", 8, "Storage Tip", "Onions should be stored in a cool, dry place but NOT next to potatoes, as they release moisture that rots onions.", 2400),
        createEntry("purple_onion", 9, "Pickled Onions", "Purple onions are the classic choice for pickling because the acid turns their color into a vibrant neon pink.", 2700),
        createEntry("purple_onion", 10, "Ancient Food", "Archaeological evidence suggests that onions have been a staple food for humans since the Bronze Age (around 3000 BC).", 3000)
    )

    val squashEntries = listOf(
        createEntry("squash", 1, "The Three Sisters", "Indigenous Americans grew squash alongside corn and beans, a system known as the Three Sisters.", 350),
        createEntry("squash", 2, "Winter vs Summer", "Winter squash (like pumpkin) have hard skins and are stored, while summer squash (like zucchini) are eaten fresh.", 700),
        createEntry("squash", 3, "Native to Americas", "Squash is one of the oldest known cultivated crops in the Western Hemisphere, dating back 10,000 years.", 1050),
        createEntry("squash", 4, "Edible Flowers", "The large, bright yellow flowers of the squash plant are edible and often stuffed or fried in many cultures.", 1400),
        createEntry("squash", 5, "Fiber and Nutrients", "Squash is packed with vitamin A, potassium, and fiber, making it excellent for digestion and vision.", 1750),
        createEntry("squash", 6, "Biodiversity", "There are hundreds of species of squash, from the tiny pattypan to the massive Atlantic Giant pumpkin.", 2100),
        createEntry("squash", 7, "Heavy Feeders", "Squash plants are 'heavy feeders', meaning they require very nutrient-rich soil and plenty of water to thrive.", 2450),
        createEntry("squash", 8, "The Largest Fruit", "The world's largest fruit is a squash variety (the pumpkin). The current record is over 1,200 kg.", 2800),
        createEntry("squash", 9, "Natural Containers", "Dried squash gourds were used by ancient people as water containers, bowls, and even musical instruments.", 3150),
        createEntry("squash", 10, "Pollination", "Squash rely heavily on bees for pollination. Without them, the plant produces small, shriveled fruit or none at all.", 3500)
    )

    val appleEntries = listOf(
        createEntry("apple", 1, "The Rose Family", "Apples are members of the Rosaceae family, making them distant cousins of the rose and the strawberry.", 400),
        createEntry("apple", 2, "Kazakhstan Roots", "The wild ancestor of the modern apple is the Malus sieversii, which still grows in the mountains of Kazakhstan.", 800),
        createEntry("apple", 3, "Floating Fruit", "Apples are 25% air, which is why they float in water. This is why 'bobbing for apples' is possible!", 1200),
        createEntry("apple", 4, "7,500 Varieties", "It would take you over 20 years to try every variety of apple in the world if you ate one per day.", 1600),
        createEntry("apple", 5, "Malic Acid", "The tartness of an apple comes from malic acid. The word 'malic' comes from the Latin word for apple, 'malum'.", 2000),
        createEntry("apple", 6, "Seed Danger", "Apple seeds contain a small amount of cyanide. However, you'd need to chew and eat hundreds to get sick.", 2400),
        createEntry("apple", 7, "Ethylene Gas", "Apples release ethylene gas, which can help ripen other fruits like bananas or tomatoes if stored together.", 2800),
        createEntry("apple", 8, "The Forbidden Fruit?", "The Bible never specifies that the fruit in Eden was an apple; it was likely a pomegranate or a fig.", 3200),
        createEntry("apple", 9, "Anti-Cholesterol", "An apple a day really might keep the doctor away! Pectin in apples helps lower 'bad' cholesterol levels.", 3600),
        createEntry("apple", 10, "Longevity", "Apple trees can live for over 100 years, although their peak production years are usually between 10 and 50.", 4000)
    )

    val plaguesEntries = listOf(
        createEntry("plagues", 1, "Aphid Infestations", "Aphids are tiny insects that suck the sap from plants, weakening them and spreading viral diseases.", 500),
        createEntry("plagues", 2, "The Potato Famine", "The Late Blight (Phytophthora infestans) caused the Great Irish Famine, destroying potato crops in the 1840s.", 1000),
        createEntry("plagues", 3, "Locust Swarms", "Desert locusts can form swarms of billions of insects, capable of eating the same amount of food as 35,000 people in a day.", 1500),
        createEntry("plagues", 4, "Spider Mites", "These tiny arachnids thrive in hot, dry conditions and can quickly cover a plant in fine, suffocating webs.", 2000),
        createEntry("plagues", 5, "Whitefly Threat", "Whiteflies secrete 'honeydew', which leads to the growth of black sooty mold on the leaves of your crops.", 2500),
        createEntry("plagues", 6, "Tomato Hornworm", "A single tomato hornworm can defoliate a whole tomato plant in just a few days if left unchecked.", 3000),
        createEntry("plagues", 7, "Damping Off", "This fungal disease causes young seedlings to collapse and die at the soil line, often due to overwatering.", 3500),
        createEntry("plagues", 8, "Caterpillar Camouflage", "Many garden pests have evolved colors that blend perfectly with leaves, making them hard for predators to find.", 4000),
        createEntry("plagues", 9, "Beneficial Insects", "Ladybugs and lacewings are a gardener's best friends, as they naturally hunt and eat harmful aphids.", 4500),
        createEntry("plagues", 10, "Climate Impact", "Warmer winters allow more pests to survive until spring, leading to larger and more frequent plague outbreaks.", 5000)
    )

    val farmersEntries = listOf(
        createEntry("farmers", 1, "The Backbone of Society", "Farmers produce almost everything we eat, yet they make up less than 2% of the population in many developed nations.", 450),
        createEntry("farmers", 2, "Soil Scientists", "Modern farmers must understand soil chemistry, moisture levels, and nutrient cycles to maintain healthy land.", 900),
        createEntry("farmers", 3, "Precision Agriculture", "Many farmers now use GPS and satellite imagery to plant seeds and apply water with centimeter-level accuracy.", 1350),
        createEntry("farmers", 4, "Family Farms", "About 90% of the world's farms are operated by families, producing about 80% of the world's food.", 1800),
        createEntry("farmers", 5, "Sustainable Practices", "Crop rotation and cover cropping are ancient techniques farmers use to prevent soil exhaustion.", 2250),
        createEntry("farmers", 6, "The Average Age", "The average age of a farmer in many countries is around 58 years, highlighting a need for young people in agriculture.", 2700),
        createEntry("farmers", 7, "Beekeepers' Partners", "Farmers often rent beehives during flowering seasons to ensure their crops are properly pollinated.", 3150),
        createEntry("farmers", 8, "Vertical Farming", "A new generation of urban farmers is growing crops in vertical stacks inside buildings to save space.", 3600),
        createEntry("farmers", 9, "Weather Gamblers", "Farming is one of the world's riskiest professions because it depends entirely on unpredictable weather patterns.", 4050),
        createEntry("farmers", 10, "Organic Certification", "Organic farmers must follow strict rules, including zero use of synthetic pesticides or fertilizers for several years.", 4500)
    )

    val pesticidesEntries = listOf(
        createEntry("pesticides", 1, "Synthetic Chemicals", "Synthetic pesticides were first widely used after World War II to increase food production globally.", 600),
        createEntry("pesticides", 2, "Natural Alternatives", "Neem oil and soapy water are effective natural pesticides that are less harmful to the environment.", 1200),
        createEntry("pesticides", 3, "Impact on Bees", "Certain pesticides, like neonicotinoids, have been linked to the decline of honeybee populations worldwide.", 1800),
        createEntry("pesticides", 4, "Bioaccumulation", "Chemicals can build up in the food chain, becoming more concentrated in animals that eat treated plants.", 2400),
        createEntry("pesticides", 5, "Pesticide Resistance", "Overuse of chemicals causes pests to evolve resistance, forcing farmers to use even stronger formulas.", 3000),
        createEntry("pesticides", 6, "Integrated Pest Management", "IPM is a strategy that uses biological controls first and chemicals only as a last resort.", 3600),
        createEntry("pesticides", 7, "Water Runoff", "Rain can wash pesticides into rivers and lakes, affecting aquatic life and contaminating drinking water.", 4200),
        createEntry("pesticides", 8, "Herbicide Use", "Herbicides are a type of pesticide used specifically to kill weeds that compete with crops for nutrients.", 4800),
        createEntry("pesticides", 9, "Safety Equipment", "Farmers must wear specialized protective gear when handling chemicals to prevent skin and lung irritation.", 5400),
        createEntry("pesticides", 10, "Regulatory Limits", "Governments set strict limits on the amount of pesticide residue allowed on fruits and vegetables sold in stores.", 6000)
    )

    val geneticEntries = listOf(
        createEntry("genetic", 1, "What is a GMO?", "Genetically Modified Organisms have had their DNA altered in a lab to introduce desirable traits.", 1000),
        createEntry("genetic", 2, "Cross-Breeding", "Humans have been 'genetically modifying' plants for thousands of years through selective breeding.", 2000),
        createEntry("genetic", 3, "Golden Rice", "Golden Rice is a GMO designed to produce vitamin A, aimed at preventing blindness in developing countries.", 3000),
        createEntry("genetic", 4, "Drought Resistance", "Scientists are developing crops that can survive with significantly less water to combat climate change.", 4000),
        createEntry("genetic", 5, "BT Crops", "BT corn produces its own natural toxin that kills specific harmful insects, reducing the need for sprayed pesticides.", 5000),
        createEntry("genetic", 6, "The Flavr Savr", "The Flavr Savr tomato was the first genetically engineered food to be sold in stores (1994).", 6000),
        createEntry("genetic", 7, "Nutritional Enhancement", "Genetic engineering can be used to make vegetables produce higher levels of vitamins and minerals.", 7000),
        createEntry("genetic", 8, "Ethical Debate", "GMOs are at the center of a debate regarding biodiversity, corporate control of seeds, and long-term health.", 8000),
        createEntry("genetic", 9, "CRISPR Technology", "CRISPR is a new tool that allows scientists to edit plant genomes much more precisely than older methods.", 9000),
        createEntry("genetic", 10, "Future of Food", "As the global population grows, genetic modification may be key to ensuring there is enough food for everyone.", 10000)
    )

    val categories = listOf(
        LibraryCategory("tomato", "Tomato", "🍅", tomatoEntries),
        LibraryCategory("broccoli", "Broccoli", "🥦", broccoliEntries),
        LibraryCategory("bell_pepper", "Bell Pepper", "🫑", bellPepperEntries),
        LibraryCategory("garlic", "Garlic", "🧄", garlicEntries),
        LibraryCategory("purple_onion", "Purple Onion", "🧅", purpleOnionEntries),
        LibraryCategory("squash", "Squash", "🥒", squashEntries),
        LibraryCategory("apple", "Apple", "🍎", appleEntries),
        LibraryCategory("plagues", "Plagues", "🐛", plaguesEntries),
        LibraryCategory("farmers", "Farmers", "👨‍🌾", farmersEntries),
        LibraryCategory("pesticides", "Pesticides", "🧪", pesticidesEntries),
        LibraryCategory("genetic", "Genetics", "🧬", geneticEntries)
    )
}
