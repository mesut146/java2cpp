#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class TreeNode: public Entry<K, V>{
//fields
public:
    static bool $assertionsDisabled;
    TreeNode<K, V>* left;
    TreeNode<K, V>* parent;
    TreeNode<K, V>* prev;
    bool red;
    TreeNode<K, V>* right;

//methods
public:
    TreeNode(int , K , V , Node<K, V>* );

    template <typename K, typename V>
    static TreeNode<K, V>* balanceDeletion(TreeNode<K, V>* , TreeNode<K, V>* );

    template <typename K, typename V>
    static TreeNode<K, V>* balanceInsertion(TreeNode<K, V>* , TreeNode<K, V>* );

    template <typename K, typename V>
    static bool checkInvariants(TreeNode<K, V>* );

    TreeNode<K, V>* find(int , java::lang::Object* , java::lang::Class<java::lang::Object*>* );

    TreeNode<K, V>* getTreeNode(int , java::lang::Object* );

    template <typename K, typename V>
    static void moveRootToFront(std::vector<Node<K, V>*>* , TreeNode<K, V>* );

    TreeNode<K, V>* putTreeVal(HashMap<K, V>* , std::vector<Node<K, V>*>* , int , K , V );

    void removeTreeNode(HashMap<K, V>* , std::vector<Node<K, V>*>* , bool );

    TreeNode<K, V>* root();

    template <typename K, typename V>
    static TreeNode<K, V>* rotateLeft(TreeNode<K, V>* , TreeNode<K, V>* );

    template <typename K, typename V>
    static TreeNode<K, V>* rotateRight(TreeNode<K, V>* , TreeNode<K, V>* );

    void split(HashMap<K, V>* , std::vector<Node<K, V>*>* , int , int );

    static int tieBreakOrder(java::lang::Object* , java::lang::Object* );

    void treeify(std::vector<Node<K, V>*>* );

    Node<K, V>* untreeify(HashMap<K, V>* );


};//class TreeNode

}//namespace java
}//namespace util
